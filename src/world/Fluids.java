package world;

public class Fluids {

	public static int WATER_SOURCE_ID;
	public static int WATER_FLOWING_START_ID;
	public static int WATER_FLOWING_END_ID;
	
	public static int LAVA_SOURCE_ID;
	public static int LAVA_FLOWING_START_ID;
	public static int LAVA_FLOWING_END_ID;
	
	public static void initFluidIDs () {
		
		WATER_SOURCE_ID = 11;
		WATER_FLOWING_START_ID = 112;
		WATER_FLOWING_END_ID = 119;
		
		LAVA_SOURCE_ID = 13;
		LAVA_FLOWING_START_ID = 120;
		LAVA_FLOWING_END_ID = 127;
		
	}
	
	public static boolean checkFluidPriority (int src, int dest) {
		
		//Water can always flow into lava, lava can always flow into water
		if ((isWater (src) && isLava (dest)) || (isLava (src) && isWater (dest))) {
			return true;
		}
		
		//Get the flow levels
		int srcFlow = getFlowLevel (src);
		int destFlow = getFlowLevel (dest);
		
		//Check if src can flow to dest
		if (destFlow + 1 < srcFlow) {
			return true;
		}
		
		//Return false in all other cases
		return false;
		
	}
	
	public static boolean isFluid (int tid) {
		return isWater (tid) || isLava (tid);
	}
	
	public static boolean isWater (int tid) {
		return tid == WATER_SOURCE_ID || (tid >= WATER_FLOWING_START_ID && tid <= WATER_FLOWING_END_ID);
	}
	
	public static boolean isWaterSource (int tid) {
		return tid == WATER_SOURCE_ID;
	}
	
	public static boolean isFlowingWater (int tid) {
		return tid >= WATER_FLOWING_START_ID && tid <= WATER_FLOWING_END_ID;
	}
	
	public static boolean isLava (int tid) {
		return tid == LAVA_SOURCE_ID || (tid >= LAVA_FLOWING_START_ID && tid <= LAVA_FLOWING_END_ID);
	}
	
	public static boolean isLavaSource (int tid) {
		return tid == LAVA_SOURCE_ID;
	}
	
	public static boolean isFlowingLava (int tid) {
		return tid >= LAVA_FLOWING_START_ID && tid <= LAVA_FLOWING_END_ID;
	}
	
	public static boolean canFlow (int tid) {
		return tid == WATER_SOURCE_ID || (tid >= WATER_FLOWING_START_ID && tid <= WATER_FLOWING_END_ID - 1) ||
				tid == LAVA_SOURCE_ID || (tid >= LAVA_FLOWING_START_ID && tid <= LAVA_FLOWING_END_ID - 1);
	}
	
	public static int getNextFlowId (int tid) {
		if (isWater (tid) && canFlow (tid)) {
			if (tid == WATER_SOURCE_ID) {
				return WATER_FLOWING_START_ID + 2;
			} else {
				return tid + 1;
			}
		}
		if (isLava (tid) && canFlow (tid)) {
			if (tid == LAVA_SOURCE_ID) {
				return LAVA_FLOWING_START_ID + 2;
			} else {
				return tid + 1;
			}
		}
		return 0;
	}
	
	public static int getFlowLevel (int tid) {
		if (tid == WATER_SOURCE_ID || tid == LAVA_SOURCE_ID) {
			return 7;
		} else {
			if (isWater (tid)) {
				return WATER_FLOWING_END_ID - tid + 1;
			} else if (isLava (tid)) {
				return LAVA_FLOWING_END_ID - tid + 1;
			} else if (tid == 0) {
				return -1; //Anything can flow into air
			} else {
				return 10; //Nothing can flow into a flow level of 10
			}
		}
	}
	
}
