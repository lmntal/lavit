import runtime.*;
import java.util.*;
public class SomeInlineCodewtCalc implements InlineCode {
	public boolean runGuard(String guardID, Membrane mem, Object obj) throws GuardNotFoundException {
		try {
		String name = "SomeInlineCodewtCalcCustomGuardImpl";

			CustomGuard cg=(CustomGuard)Class.forName(name).newInstance();

			if(cg==null) throw new GuardNotFoundException();

			return cg.run(guardID, mem, obj);

		} catch(GuardNotFoundException e) {
			throw new GuardNotFoundException();

		} catch(ClassNotFoundException e) {
		} catch(InstantiationException e) {
		} catch(IllegalAccessException e) {
		} catch(Exception e) {

			e.printStackTrace();

		}

		throw new GuardNotFoundException();

	}
	public void run(Atom me, int codeID) {
		Membrane mem = me.getMem();
		switch(codeID) {
		case 1: {
			/*inline*/
    int i = Integer.parseInt(me.nth(0));
    String op = me.nthAtom(1).toString();
    if(op.equals("=")){
      System.out.print(" =\n"+i+"\n");
    }else{
      System.out.print(" "+op+" ");
    }
    mem.removeAtom(me.nthAtom(0));
    mem.removeAtom(me.nthAtom(1));
    mem.removeAtom(me); 
  
			break; }
		case 0: {
			/*inline*/
    int i = Integer.parseInt(me.nth(0));
    String pre = me.nthAtom(1).toString();
    if(pre.equals("=")) System.out.print("\n");
    System.out.print(i);
    mem.removeAtom(me.nthAtom(0));
    mem.removeAtom(me.nthAtom(1));
    mem.removeAtom(me); 
  
			break; }
		case 2: {
			/*inline*/
    System.out.print("\n\n");
    mem.removeAtom(me); 
  
			break; }
		}
	}
}
