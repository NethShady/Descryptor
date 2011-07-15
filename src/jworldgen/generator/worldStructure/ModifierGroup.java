package jworldgen.generator.worldStructure;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import jworldgen.exceptionHandler.CriticalFailure;
import jworldgen.exceptionHandler.ExceptionLogger;
import jworldgen.exceptionHandler.LoggerLevel;
import jworldgen.exceptionHandler.UnknownIdentifier;
import jworldgen.generator.RNG;
import jworldgen.generator.VariableResolver;
import jworldgen.generator.World;
import jworldgen.generator.worldStructure.modifiers.ChangeType;
import jworldgen.generator.worldStructure.modifiers.MetaballModifier;
import jworldgen.generator.worldStructure.modifiers.Modifier;
import jworldgen.generator.worldStructure.modifiers.PerlinModifier;
import jworldgen.generator.worldStructure.modifiers.VoronoiModifier;
import jworldgen.generator.worldStructure.modifiers.WeightedPerlinModifier;
import jworldgen.generator.worldStructure.modifiers.WorleyModifier;
import jworldgen.parser.parseStructure.ParseAssignment;

public class ModifierGroup {
	protected String identifier;
	protected RNG rng;
	protected int minX,minY,minZ,maxX,maxY,maxZ;
	protected Hashtable<String,Modifier> modifiers;
	private Hashtable<Integer,Integer> probabilities;
	private ArrayList<String> modifierNames;
	private Hashtable<Integer,Integer> typeIDs;
	
	protected ArrayList<ParseAssignment> assignments;
	protected int probSum;
	protected ChangeType changeType;
	
	public ModifierGroup(String identifier, ArrayList<ParseAssignment> assignments, Hashtable<Integer,Integer> probabilities, Hashtable<Integer,Integer> typeIDs, ArrayList<String> modifierNames, ChangeType changeType)
	{
		this.identifier = identifier;
		this.assignments = assignments;
		this.probabilities = probabilities;
		this.modifierNames = modifierNames;
		this.typeIDs = typeIDs;
		this.changeType = changeType;
		probSum = 0;
		for (Enumeration<Integer> e = probabilities.keys(); e.hasMoreElements();)
		{
			probSum += probabilities.get(e.nextElement());
		}
	}
	
	public ModifierGroup(String identifier, ArrayList<ParseAssignment> assignments, Hashtable<Integer,Integer> probabilities, Hashtable<Integer,Integer> typeIDs, Hashtable<String,Modifier> modifiers, ChangeType changeType)
	{
		this.identifier = identifier;
		this.assignments = assignments;
		this.probabilities = probabilities;
		this.modifiers = modifiers;
		this.typeIDs = typeIDs;
		this.changeType = changeType;
		probSum = 0;
		for (Enumeration<Integer> e = probabilities.keys(); e.hasMoreElements();)
		{
			probSum += probabilities.get(e.nextElement());
		}
	}
	
	public void setRNG(RNG rng)
	{
		this.rng = rng;
	}
	
	public void setLocation(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
	{
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}
	
	public ModifierGroup clone()
	{
		return new ModifierGroup(identifier,assignments,probabilities,typeIDs,modifiers,changeType);
	}
	
	public ChangeType getChangeType()
	{
		return changeType;
	}
	
	public void addModifiers(Hashtable<String,Modifier> modifiers)
	{
		this.modifiers = new Hashtable<String,Modifier>();
		for (String modName : modifierNames)
		{
			if (modifiers.containsKey(modName))
			{
				this.modifiers.put(modName, modifiers.get(modName));
			}
			else
			{
				try {
					ExceptionLogger.logException(new UnknownIdentifier(modName), LoggerLevel.ERROR);
				} catch (CriticalFailure e) {
					//Should not be reachable
				}
			}
		}
	}
	public void prepareForFilling (RNG rng, World world)
	{
		for (Enumeration<String> e = modifiers.keys(); e.hasMoreElements();)
		{
			Modifier mod = modifiers.get(e.nextElement());
			switch(mod.getType())
			{
			case PERLIN:
				((PerlinModifier) mod).setRNG(rng, Math.max(world.getWidth(),world.getHeight()));
				mod.setLocation(minX, minY, minZ, maxX, maxY, maxZ);
				break;
			case WEIGHTED_PERLIN:
				((WeightedPerlinModifier) mod).setRNG(rng, Math.max(world.getWidth(),world.getHeight()));
				mod.setLocation(minX, minY, minZ, maxX, maxY, maxZ);
				break;
			case METABALL:
				mod.setRNG(rng);
				((MetaballModifier) mod).setLocation(minX, minY, minZ, maxX, maxY, maxZ);
				break;
			case VORONOI:
				mod.setRNG(rng);
				((VoronoiModifier) mod).setLocation(minX, minY, minZ, maxX, maxY, maxZ);
				break;
			case WORLEY:
				mod.setRNG(rng);
				((WorleyModifier) mod).setLocation(minX, minY, minZ, maxX, maxY, maxZ);
				break;
			}
		}
	}
	public int getValue(int x, int y, int z)
	{
		VariableResolver resolver = new VariableResolver();
		for (Enumeration<String> e = modifiers.keys(); e.hasMoreElements();)
		{
			String key = e.nextElement();
			resolver.setVariable(key, modifiers.get(key).getValue(x, y, z));
		}
		resolver.setVariable("xPos", x);
		resolver.setVariable("yPos", y);
		resolver.setVariable("zPos", z);
		resolver.setVariable("minX", minX);
		resolver.setVariable("minY", minY);
		resolver.setVariable("minZ", minZ);
		resolver.setVariable("maxX", maxX);
		resolver.setVariable("maxY", maxY);
		resolver.setVariable("maxZ", maxZ);
		if (assignments != null)
		{
			for (ParseAssignment assignment : assignments)
			{
				assignment.evaluate(rng, resolver);
			}
		}
		int curProb = 0;
		for (Enumeration<Integer> e = probabilities.keys(); e.hasMoreElements();)
		{
			int key = e.nextElement();
			curProb += probabilities.get(key);
			if (curProb/(float)probSum >= resolver.getVariable("result").floatValue())
			{
				return typeIDs.get(key);
			}
		}
		return typeIDs.get(1);
	}
}
