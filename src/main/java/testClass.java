/**
 * Created by karltrout on 10/11/17.
 */
public class testClass {

    public testClass() {
    }

    public static void  main(String[] args){

        testClass t = new testClass();
        //obj -> !t.send(obj);
    }

    public boolean send(ObjA a){
        return false;
    }

    /*public boolean send(ObjB b){
        return false;
    }*/

    private class ObjA {

    }

    private class ObjB{

    }

    private interface IObj {
        public boolean send(ObjA a);
        public boolean send(ObjB b);
    }
}
