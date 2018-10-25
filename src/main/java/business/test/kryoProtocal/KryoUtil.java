package business.test.kryoProtocal;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.io.Input;

import java.io.ByteArrayOutputStream;

public class KryoUtil{

    private BusinessKryoFactory factory = new BusinessKryoFactory();

    public byte[]  doSerializable(Object obj,Class c){
        Kryo kryo = null;
        try{
            kryo  = factory.getPool().borrow();

            kryo.setReferences(false);
            kryo.register(c);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Output output2 = new Output(baos);
            kryo.writeClassAndObject(output2, obj);
            output2.flush();
            output2.close();

            baos.flush();
            baos.close();


            byte[] b = baos.toByteArray();
            return b;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(kryo != null){
                factory.getPool().release(kryo);
            }
        }
        return null;
    }

    public Object parseSerializable(byte[] b,Class c){
        Kryo kryo = null;
        try{
            kryo  = factory.getPool().borrow();
            kryo.setReferences(false);
            kryo.register(c);

            Input input = new Input(b);
            input.read(b);

            input.close();
            Object obj = kryo.readClassAndObject(input);
            System.out.println("----"+obj);
            return obj;
        }catch (Exception e){

        }finally {
            if(kryo != null){
                factory.getPool().release(kryo);
            }
        }
        return null;
    }
}
