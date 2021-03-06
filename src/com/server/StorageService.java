package com.server;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipInputStream;

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

    public static void main(String[] args) throws Exception {
        StorageService test=new StorageService("maxim");
        test.Zip("!server\\test1","arch.zip");
        test.Unzip("!server\\arch.zip","!server\\vasia");
    }





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
        if(isPapka){
            try {
                fileATrans(InputStream,name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            try {
                fileTrans(InputStream,name);
            } catch (IOException e) {
                e.printStackTrace();
                return 297;
            }
        }
        return 0;
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


    private void fileATrans(DataInputStream dataInputStream,String name) throws IOException {
        String aName=relRoot+System.currentTimeMillis()+".zip";
        File file=new File(aName);
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
        fos.close();
        try {
            Unzip(aName,relRoot + name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        file.delete();
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
        if (flag) file.delete();
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
        zout.setMethod(ZipOutputStream.STORED);
        //шобы русские работали(не точно)
        zout.setEncoding("CP866");

        File fileSource = new File(sourceDir);

        zout.setMethod(ZipOutputStream.DEFLATED);
        zout.setLevel(0);

        addDirectory(zout,fileSource, "");

        zout.close();
        return "!server\\" + zipFile;
    }

    private void addDirectory(ZipOutputStream zout, File fileSource, String Entry) throws IOException {
        File[] files = fileSource.listFiles();
        System.out.println("Добавление директории <" + fileSource.getName() + ">");
        for(int i=0;i<files.length;i++){
            if(files[i].isDirectory()){
                addDirectory(zout,files[i], !Entry.equals("") ? Entry + "\\" + files[i].getName() : files[i].getName());
                continue;
            }
            System.out.println("Добавление файла <" + (!Entry.equals("") ? Entry + "\\" + files[i].getName() : files[i].getName()) + ">");

            FileInputStream fis =new FileInputStream(files[i]);

            zout.putNextEntry(new ZipEntry(!Entry.equals("") ? Entry + "\\" + files[i].getName() : files[i].getName()));

            byte[] buffer = new byte[4048];
            int lenght = 0;
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
        String fileZip = "src/main/resources/unzipTest/compressed.zip";
        File destDir = new File("src/main/resources/unzipTest");
        File pathF = new File(path);
        if (!pathF.exists()) pathF.mkdir();
        else return;
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipDir));
        try (zis) {
            java.util.zip.ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                File file = new File(pathF, entry.getName());

                if (!file.toPath().normalize().startsWith(pathF.toPath())) {
                    throw new IOException("Bad zip entry");
                }

                if (entry.isDirectory()) {
                    file.mkdirs();
                    continue;
                }

                byte[] buffer = new byte[4004];
                file.getParentFile().mkdirs();
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                int count;

                while ((count = zis.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }

                out.close();
            }
        }
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