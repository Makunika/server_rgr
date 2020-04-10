package com.server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;



import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class StorageService {

    static final Path SERVER_ROOT= Paths.get("D:\\LAB BLa\\!server");

    private long storageAll=15360;
    private long storageFill=0;
    private Path root;




    /**
     * Передаем логин
     * @param Root
     */
    StorageService(String Root)  {
        root=Paths.get(SERVER_ROOT.toString()+"\\"+Root);
        if(!Files.isDirectory(root)) {
            try {
                Files.createDirectory(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            storageFill=Files.walk(root,Integer.MAX_VALUE)
                    .filter(p -> p.toFile().isFile())
                    .mapToLong(p -> p.toFile().length())
                    .sum();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SendStoreInfo();
    }

    /**
     * Полный путь файла SERVER_ROOT\login\...
     * Размер передаваемого файла
     * Инпут с сокета
     * @param fileName
     * @param size
     * @param in
     * @throws IOException
     */
    void AddTo(String fileName,long size, DataInputStream in) throws IOException {
        OutputStream output = new FileOutputStream(fileName);
        byte[] buffer = new byte[8008];
        int bytesRead;
        while (size > 0 && (bytesRead = in.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1)
        {
            output.write(buffer, 0, bytesRead);
            size -= bytesRead;
        }
    }

    /**
     * Путь в котором будем создавать
     * Папка которую будем создавать
     * @param path
     * @param name
     * @throws IOException
     */
    void AddCatalog(String path,String name) {
        try {
            Files.createDirectory(Paths.get(path+"//" + name));
        } catch (IOException e) {
           // System.out.println("Already exist");
        }
    }

    /**
     * Начальный путь и конечный путь
     * @param curPath
     * @param newPath
     */
    void Relocate(String curPath,String newPath) {
        try {
            Files.move(Paths.get(curPath),Paths.get(newPath),ATOMIC_MOVE,REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Тут мы сразу после входа составим всю инфу о всех файлах и путях а так же занимаемом месте в строку
     *  и вернем в client чтобы отправить пользователю
     *
     */
    void SendStoreInfo() {

    }

    /**
     * Путь по которому лежит файл/дериктория + новое имя(расширение обязательно указывать)
     * @param path
     * @param newName
     */
    public void Rename(String path,String newName){
       Path f=Paths.get(path);
       Path rf=Paths.get(f.getParent()+"\\"+newName);
        try {
            Files.move(f,rf,REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
