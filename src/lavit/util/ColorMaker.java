package lavit.util;

import java.awt.Color;
import java.util.ArrayList;

public class ColorMaker {
	static ArrayList<Color> colors = null;

	public static void colorPreparation(){
		if(colors==null){
			colors = new ArrayList<Color>();
			for(int r : new int[]{0,128,255}){
				for(int g : new int[]{0,128,255}){
					for(int b : new int[]{0,128,255}){
						colors.add(new Color(r,g,b));
					}
				}
			}
		}
	}

	public static Color getColor(int i){
		colorPreparation();
		return colors.get(i%colors.size());
	}

	public static Color getColor(String str){
		int s=1;
		for(int i=0;i<str.length();++i){
			char c = str.charAt(i);
			if(isNumber(c)){
				if(i>=1&&isNumber(str.charAt(i-1))){ continue; }
				s+=1;
			}else if(c=='-'){
				s+=0;
			}else{
				s+=c;
			}
		}
		return getColor(s);
	}

	public static boolean isNumber(char c){
		if('0'<=c&&c<='9'){
			return true;
		}else{
			return false;
		}
	}

}
