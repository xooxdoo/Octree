package org.ninthworld.octree;

import java.util.List;
import org.lwjgl.util.vector.Vector3f;

public class OctNode {
	public Integer[][][] array;
	public OctNode[] children;
	public Vector3f offset;
	public int width, data;
	
	public OctNode(Integer[][][] array, int width, Vector3f offset){
		this.array = array;
		this.width = width;
		this.offset = offset;
	}
	
	public void generate(Debug lookup){
		int value = -1;
		for(int x=0; x<width; x++){
			for(int y=0; y<width; y++){
				for(int z=0; z<width; z++){
					lookup.num++;
					int store = array[x+(int)offset.x][y+(int)offset.y][z+(int)offset.z];
					if(value < 0)
						value = store;
					if(store != value){
						children = new OctNode[8];
						for(int i=0; i<children.length; i++){
							Vector3f offS = new Vector3f(offset.x + (width/2f)*(i%2), offset.y + (width/2f)*((int)Math.floor(i/2f)%2), offset.z + (width/2f)*((int)Math.floor(i/4f)%2));
							children[i] = new OctNode(array, (int)(width/2f), offS);
							children[i].generate(lookup);
						}
					}
				}
			}
		}
		if(children == null){
			data = value;
		}
	}
	
	public void toArrayList(List<OctNode> list){
		if(children != null){
			for(OctNode node : children){
				node.toArrayList(list);
			}
		}else{
			list.add(this);
		}
	}
}
