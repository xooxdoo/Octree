package org.ninthworld.octree;

import org.lwjgl.util.vector.Vector3f;

public class Octree extends OctNode {

	public Octree(Integer[][][] array) {
		super(array, array.length, new Vector3f(0, 0, 0));
	}
	
}
