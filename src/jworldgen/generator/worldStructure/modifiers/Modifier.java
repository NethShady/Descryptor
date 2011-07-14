package jworldgen.generator.worldStructure.modifiers;

import java.util.ArrayList;

import jworldgen.generator.RNG;
import jworldgen.parser.parseStructure.ParseAssignment;

public abstract class Modifier {
	
	protected String identifier;
	protected RNG rng;
	protected ModifierType type;
	protected int minX,minY,minZ,maxX,maxY,maxZ;
	
	protected ArrayList<ParseAssignment> assignments;
	
	public Modifier(String identifier, ArrayList<ParseAssignment> assignments)
	{
		this.identifier = identifier;
		this.assignments = assignments;
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
	
	public abstract Modifier clone();
	
	public abstract float getValue(int x, int y, int z);
	
	public ModifierType getType()
	{
		return type;
	}
}
