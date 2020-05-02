package com.server;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class StorageService {

    static final Path SERVER_ROOT= Paths.get("!server");
    static final String ARCH_PLACE="ARCH_TEMP";
    static final String SLASH="\\";
    private static ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
    private long storageAll=15360;
    private long storageFill=0;
    private Path root;
    private String relRoot;
    private StringBuffer tree;
    private int BREAKER;

    public String newTrans[];
    public String inStr;
/*
    public static void main(String[] args) throws Exception {
        StorageService test=new StorageService("maxim");
       test.GetTree();
        System.out.println(test.tree.toString());
        Random random=new Random(1);
        for(int i=0;i<10000;i++) {
            byte[] a=new byte[8];
            random.nextBytes(a);
            System.out.println(bytesToLong(a));
        }

    }
*/




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
        relRoot="!server\\"+Root;
    }



    public String GetSize()
    {
        try {
            storageFill=Files.walk(root,Integer.MAX_VALUE)
                    .filter(p -> p.toFile().isFile())
                    .mapToLong(p -> p.toFile().length())
                    .sum();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Long.toString(storageFill);
    }

    public String GetSize(String path)
    {
        Path path1 = Paths.get(relRoot + path);
        try {
            storageFill=Files.walk(path1,Integer.MAX_VALUE)
                    .filter(p -> p.toFile().isFile())
                    .mapToLong(p -> p.toFile().length())
                    .sum();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Long.toString(storageFill);
    }


    public String GetTree() {
        tree=new StringBuffer("");
        File fileSource=new File(root.toString());
        BREAKER = 0;
        RecTree(fileSource);
        return tree.toString();
    }
    private void RecTree(File fileSource) {
        File[] files = fileSource.listFiles();
        for(int i=0;i<files.length;i++) {
            try {
                BasicFileAttributes atr=Files.readAttributes(files[i].toPath(),BasicFileAttributes.class);
                if(atr.isDirectory()){
                    tree.append(BREAKER + "\t" +files[i].getName()+"\t"+"-1\\"+atr.creationTime()+"\n");
                    BreakerUp();
                    RecTree(files[i]);
                    BreakerDown();
                }else{
                    tree.append(BREAKER + "\t" +files[i].getName()+"\t"+files[i].length()+"\\"+atr.creationTime()+"\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void BreakerUp(){
        BREAKER++;
    }
    private void BreakerDown(){
        BREAKER--;
    }

    public int prepairTrans(DataInputStream InputStream,String name,long size,boolean isPapka){
        //if(storage.storageFill+size>storage.storageAll) return 298;
        if(isPapka){ }
        else{
            try {
                fileTrans(InputStream,name);
            } catch (IOException e) {
                e.printStackTrace();
                return 297;
            }
            return 0;
        }
        return -1;
    }
    private void fileTrans(DataInputStream dataInputStream,String name) throws IOException {
        File file=new File(relRoot+name);
        file.createNewFile();
        FileOutputStream fos=new FileOutputStream(file);
        BufferedInputStream bis = new BufferedInputStream(dataInputStream);

        byte buffer[]=new byte[8008];
        byte size[]=new byte[8];
        bis.read(size);
        long sizel=bytesToLong(size);

        while (sizel > 0) {
            int i = bis.read(buffer);
            fos.write(buffer, 0, i);
            sizel-= i;
        }
        bis.close();
    }

    public long Remove(String parh){
        File file= new File(relRoot + parh);
        long size;
        if (file.exists())
        {
            if (file.isDirectory())
            {
                size = Long.parseLong(GetSize(parh));
                recursiveDelete(file);
                return size;
            }
            else
            {
                size = file.length();

                if (file.delete())
                    return size;
                return -1;
            }
        }
        else
        {
            return -1;
        }
    }

    private void recursiveDelete(File file) {
        // до конца рекурсивного цикла
        if (!file.exists())
            return;

        //если это папка, то идем внутрь этой папки и вызываем рекурсивное удаление всего, что там есть
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                // рекурсивный вызов
                recursiveDelete(f);
            }
        }
        // вызываем метод delete() для удаления файлов и пустых(!) папок
        file.delete();
        System.out.println("Удаленный файл или папка: " + file.getAbsolutePath());
    }


    public boolean OutTrans(BufferedOutputStream bos,String name) throws IOException {
        File file=new File(relRoot+name);
        if(!file.exists()) return false;
        boolean flag = false;
        if (file.isDirectory())
        {
            file = new File(Zip(relRoot + name, name + ".zip"));
            flag = true;
        }
        BufferedInputStream oif = new BufferedInputStream(new FileInputStream(file));
        bos.write(longToBytes(file.length()));
        byte[] buffer = new byte[8192];
        int i = 0;
        while ((i = oif.read(buffer)) != -1) {
            bos.write(buffer, 0, i);
        }
        bos.flush();
        oif.close();
        //if (flag) file.delete();
        return true;
    }


    /**
     * Путь в котором будем создавать
     * Папка которую будем создавать
     * @param path
     * @param name
     * @throws IOException
     */
    public boolean AddCatalog(String path,String name) {
        try {
            Files.createDirectory(Paths.get(relRoot + path+"\\" + name));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Начальный путь и конечный путь
     * @param curPath
     * @param newPath
     */
    public boolean Relocate(String curPath,String newPath) {
        try {
            Files.move(Paths.get(relRoot + curPath),Paths.get(relRoot + newPath),ATOMIC_MOVE,REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * Путь по которому лежит файл/дериктория + новое имя(расширение обязательно указывать)
     * @param path
     * @param newName
     */
    public boolean Rename(String path,String newName){
        Path f=Paths.get(relRoot + path);
        Path rf=Paths.get(f.getParent()+"\\"+newName);
        try {
            Files.move(f,rf,REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Принимает относительный путь и путь для создания архива
     * @param sourceDir
     * @param zipFile
     * @throws IOException
     */
    public String Zip(String sourceDir, String zipFile) throws IOException {
        FileOutputStream fout=new FileOutputStream("!server\\"+zipFile);
        ZipOutputStream zout = new ZipOutputStream(fout);

        //шобы русские работали(не точно)
        zout.setEncoding("CP866");

        File fileSource = new File(sourceDir);

        addDirectory(zout,fileSource);

        zout.close();
        return "!server\\" + zipFile;
    }

    private void addDirectory(ZipOutputStream zout, File fileSource) throws IOException {
        File[] files = fileSource.listFiles();
        System.out.println("Добавление директории <" + fileSource.getName() + ">");
        for(int i=0;i<files.length;i++){
            if(files[i].isDirectory()){
                addDirectory(zout,files[i]);
                continue;
            }
            System.out.println("Добавление файла <" + files[i].getName() + ">");

            FileInputStream fis =new FileInputStream(files[i]);

            zout.putNextEntry(new ZipEntry(files[i].getPath()));

            byte[] buffer = new byte[4048];
            int lenght;
            while((lenght=fis.read(buffer))>0)
                zout.write(buffer,0,lenght);
            zout.closeEntry();
            fis.close();
        }
    }

    private void createDir(final String dir) {
        File file = new File(dir);
        if (!file.exists())
            file.mkdirs();
    }
    private void createFolder(final String dirName) {
        if (dirName.endsWith(SLASH))
            createDir(dirName.substring(0, dirName.length() - 1));
    }
    private void checkFolder(final String file_path) {
        if (!file_path.endsWith(SLASH) && file_path.contains(SLASH)) {
            String dir = file_path.substring(0, file_path.lastIndexOf(SLASH));
            createDir(dir);
        }
    }

    /**
     * ПУть АРхива и куда его созать
     * @param zipDir
     * @param path
     * @throws Exception
     */
    private void Unzip(String zipDir,String path) throws Exception {
        ZipFile zipFile = new ZipFile(zipDir, "CP866");
        Enumeration<?> entries = zipFile.getEntries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            String entryName = path+SLASH+entry.getName();
            if (entryName.endsWith(SLASH)) {
                System.out.println("Создание директории <" + entryName + ">");
                createFolder (entryName);
                continue;
            } else
                checkFolder(entryName);
            System.out.println("Чтение файла <" + entryName + ">");
            InputStream  fis = (InputStream) zipFile.getInputStream(entry);

            FileOutputStream fos = new FileOutputStream(entryName);
            byte[] buffer = new byte[fis.available()];
            // Считываем буфер
            fis.read(buffer, 0, buffer.length);
            // Записываем из буфера в файл
            fos.write(buffer, 0, buffer.length);
            fis.close();
            fos.close();
        }
        zipFile.close() ;
        System.out.println("Zip файл разархивирован!");
    }
    public static long bytesToLong(byte[] bytes) {
        long result = 0;
        for (int i = 0; i < Long.BYTES; i++) {
            result <<= Long.BYTES;
            result |= (bytes[i] & 0xFF);
        }
        return result;
    }
    private static byte[] longToBytes(long x) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte)(x & 0xFF);
            x >>= 8;
        }
        return result;
    }
}