package org.ninthworld.octree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.lwjgl.opengl.GL11;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class Main {
		
	public boolean closeRequested;
	private long lastFrame;
	private long lastFPS;
	private int fps;
	
	private Camera camera;
	
	float voxelSize = 8;
	int chunkW = 16;
	Integer[][][] chunk = new Integer[chunkW][chunkW][chunkW];
	Octree octree;
	List<OctNode> finalNodes;
	
	private void initialize() {
		initDisplay(1080, 640);
		initGL();
		getDelta();
		lastFPS = getTime();
		
		// Initialize Camera

		camera = new Camera();
		camera.create();
		camera.setY(16);
		
		// Initialize Chunks
		
		for(int x=0; x<chunkW; x++){
			for(int y=0; y<chunkW; y++){
				for(int z=0; z<chunkW; z++){
					if(y == 9){
						if(Math.random() > .8){
							chunk[x][y][z] = 1;
						}else{
							chunk[x][y][z] = 0;
						}
					}else if(y < 9){
						chunk[x][y][z] = 1;
					}else{
						chunk[x][y][z] = 0;
					}
				}
			}
		}
		
		// Initialize Octree Object
		
		octree = new Octree(chunk);
		
		// Generate Octree
		
		Debug lookup = new Debug();
		octree.generate(lookup);
		System.out.println(lookup.num);
		
		// Generate an ArrayList of all the nodes without children to be drawn
		finalNodes = new ArrayList<OctNode>();
		octree.toArrayList(finalNodes);
		
		while(!closeRequested){
			int delta = getDelta();
			
			camera.acceptInput(delta);
			pollInput();
			update(delta);
			
			camera.apply();
			renderGL();

			Display.update();
			Display.sync(60);
		}
		cleanUp();	
	}
	
	private void pollInput(){
		while(Keyboard.next()) {
		    if(Keyboard.getEventKeyState()){
		    	if(Keyboard.getEventKey() == Keyboard.KEY_ESCAPE)
					closeRequested = true;
		    }else{
		    }
		}
		
		if(Display.isCloseRequested())
			closeRequested = true;
	}
	
	private void update(int delta){
		updateFPS();
	}
	
	private void renderGL(){
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		/*for(int x=0; x<chunkW; x++){
			for(int y=0; y<chunkW; y++){
				for(int z=0; z<chunkW; z++){
					drawBlockGeometry(new Vector3f(x*voxelSize, y*voxelSize, z*voxelSize), voxelSize);
				}
			}
		}*/
		
		for(OctNode node : finalNodes){
			if(node.data > 0){
				drawBlockGeometry(new Vector3f(node.offset.x*voxelSize, node.offset.y*voxelSize, node.offset.z*voxelSize), node.width*voxelSize, false, new Color(1, 0, 0));
			}else{
				drawBlockGeometry(new Vector3f(node.offset.x*voxelSize, node.offset.y*voxelSize, node.offset.z*voxelSize), node.width*voxelSize, true, new Color(1, 1, 1));
			}
		}		
	}
	
	private void cleanUp(){
		Display.destroy();
		System.exit(0);
	}
	
	private void initDisplay(int width, int height){
		try {
			Display.setDisplayMode(new DisplayMode(width, height));
			Display.setVSyncEnabled(true);
			Display.create(new PixelFormat(0, 16, 1));
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void initGL(){
		int width = Display.getDisplayMode().getWidth();
		int height = Display.getDisplayMode().getHeight();

		GL11.glViewport(0, 0, width, height);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluPerspective(45.0f, ((float) width / (float) height), 0.1f, 10000.0f);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();

		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glClearColor(0.14f, 0.48f, 0.64f, 1.0f);
		GL11.glClearDepth(1.0f);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_FRONT);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
	}
	

	public long getTime() {
		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}
	
	public int getDelta() {
		long time = getTime();
		int delta = (int) (time - lastFrame);
		lastFrame = time;
		
		return delta;
	}
	
	public void updateFPS() {
		if(getTime() - lastFPS > 1000){
			Display.setTitle("FPS: " + fps);
			fps = 0;
			lastFPS += 1000;
		}
		fps++;
	}
	
	public static void main(String[] args){
		Main main = new Main();
		main.initialize();
	}
	

	public static void drawFace(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4, Vector3f n, boolean wireframe, Color color){
		GL11.glColor3f(color.r, color.g, color.b);
		GL11.glBegin((wireframe ? GL11.GL_LINE_LOOP : GL11.GL_QUADS));
			GL11.glNormal3f(n.x, n.y, n.z);
			GL11.glVertex3f(p1.x, p1.y, p1.z);
			GL11.glNormal3f(n.x, n.y, n.z);
			GL11.glVertex3f(p2.x, p2.y, p2.z);
			GL11.glNormal3f(n.x, n.y, n.z);
			GL11.glVertex3f(p3.x, p3.y, p3.z);
			GL11.glNormal3f(n.x, n.y, n.z);
			GL11.glVertex3f(p4.x, p4.y, p4.z);
		GL11.glEnd();
	}
	
	public static void drawBlockGeometry(Vector3f p1, float size, boolean wireframe, Color color){
		/*
			    v1 _________ v2       y
				  /|       /|         |
			  v3 /_|___ v4/ |         |_____ z
				|  |v5____|_| v6     /
				| /       | /       / x
			 v7 |/________|/ v8      	
		*/	
		
		Vector3f p2 = new Vector3f(p1.x + size, p1.y + size, p1.z + size);
		
		// Corners
		Vector3f v1 = new Vector3f(p1.x, p1.y, p1.z);
		Vector3f v2 = new Vector3f(p1.x, p1.y, p2.z);
		Vector3f v3 = new Vector3f(p2.x, p1.y, p1.z);
		Vector3f v4 = new Vector3f(p2.x, p1.y, p2.z);
		Vector3f v5 = new Vector3f(p1.x, p2.y, p1.z);
		Vector3f v6 = new Vector3f(p1.x, p2.y, p2.z);
		Vector3f v7 = new Vector3f(p2.x, p2.y, p1.z);
		Vector3f v8 = new Vector3f(p2.x, p2.y, p2.z);
		
		// Normals
		Vector3f top = new Vector3f(0f, 1f, 0f);
		Vector3f bottom = new Vector3f(0f, -1f, 0f);
		Vector3f front = new Vector3f(1f, 0f, 0f);
		Vector3f back = new Vector3f(-1f, 0f, 0f);
		Vector3f left = new Vector3f(0f, 0f, -1f);
		Vector3f right = new Vector3f(0f, 0f, 1f);
		
		// Drawing
		drawFace(v1, v2, v4, v3, top, wireframe, color); // TOP
		drawFace(v7, v8, v6, v5, bottom, wireframe, color); // BOTTOM
		drawFace(v3, v4, v8, v7, front, wireframe, color); // FRONT
		drawFace(v2, v1, v5, v6, back, wireframe, color); // BACK
		drawFace(v1, v3, v7, v5, left, wireframe, color); // LEFT
		drawFace(v4, v2, v6, v8, right, wireframe, color); // RIGHT
	}
}
