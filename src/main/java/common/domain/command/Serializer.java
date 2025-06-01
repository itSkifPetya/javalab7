//package common.domain.command;
//
//import java.io.*;
//
//public class Serializer {
//    private static Serializer instance;
//    private Serializer() {}
//
//    public static Serializer getInstance() {
//        if (instance == null) instance = new Serializer();
//        return instance;
//    }
//
//    public byte[] serialize(Object object) throws IOException {
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        ObjectOutputStream oos = new ObjectOutputStream(bos);
//        oos.writeObject(object);
//        oos.flush();
//        return bos.toByteArray();
//    }
//
//    public Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
//        ByteArrayInputStream bis = new ByteArrayInputStream(data);
//        ObjectInputStream ois = new ObjectInputStream(bis);
//        return ois.readObject();
//    }
//
//    public Object deserialize(InputStream is) throws IOException, ClassNotFoundException {
//        ObjectInputStream ois = new ObjectInputStream(is);
//        return ois.readObject();
//    }
//
//    public void serialize(Object object, OutputStream os) throws IOException {
//        ObjectOutputStream oos = new ObjectOutputStream(os);
//        oos.writeObject(object);
//        oos.flush();
//    }
///*
//
//    public byte[] serialize(Object o) throws IOException {
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        ObjectOutputStream oos = new ObjectOutputStream(bos);
//        oos.writeObject(o);
//        oos.flush();
//        return bos.toByteArray();
//    }
//
//    public Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
//        ByteArrayInputStream bis = new ByteArrayInputStream(data);
//        ObjectInputStream ois = new ObjectInputStream(bis);
//        return ois.readObject();
//    }
//*/
//
//
//
//}
package common.domain.command;

import java.io.*;

public class Serializer {
    private static Serializer instance;

    private Serializer() {}

    public static Serializer getInstance() {
        if (instance == null) instance = new Serializer();
        return instance;
    }

    // Сериализация с добавлением длины данных в начало
    public byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(object);
        oos.flush();
        byte[] data = bos.toByteArray();

        // Создаем новый поток с длиной в начале
        ByteArrayOutputStream framedBos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(framedBos);
        dos.writeInt(data.length); // записываем длину данных
        dos.write(data);           // записываем сами данные
        return framedBos.toByteArray();
    }

    // Десериализация массива байт (с учетом длины в начале)
    public Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bis);

        // Читаем длину данных
        int length = dis.readInt();
        if (length != data.length - 4) {
            throw new IOException("Invalid data length");
        }

        // Читаем сами данные
        byte[] objectData = new byte[length];
        dis.readFully(objectData);

        ByteArrayInputStream objectBis = new ByteArrayInputStream(objectData);
        ObjectInputStream ois = new ObjectInputStream(objectBis);
        return ois.readObject();
    }

    // Десериализация из потока (для сервера, работающего с каналами)
    public Object deserialize(InputStream is) throws IOException, ClassNotFoundException {
        DataInputStream dis = new DataInputStream(is);

        // Читаем длину данных
        int length = dis.readInt();

        // Читаем сами данные
        byte[] objectData = new byte[length];
        dis.readFully(objectData);

        ByteArrayInputStream bis = new ByteArrayInputStream(objectData);
        ObjectInputStream ois = new ObjectInputStream(bis);
        return ois.readObject();
    }

    // Сериализация в поток с добавлением длины (для клиента)
    public void serialize(Object object, OutputStream os) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(object);
        oos.flush();
        byte[] data = bos.toByteArray();

        // Записываем длину и данные в выходной поток
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeInt(data.length);
        dos.write(data);
        dos.flush();
    }
}