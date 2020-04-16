package com.server;

import org.apache.tools.zip.*;

import java.io.*;
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

    private long storageAll=15360;
    private long storageFill=0;
    private Path root;
    private String relRoot;
    private StringBuffer tree;
    private int BREAKER;
    private int breaker=0;
    public static void main(String[] args) throws Exception {
        StorageService test=new StorageService("maxim");
        test.GetTree();
        System.out.println(test.tree.toString());
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

    public String GetTree()
    {
        tree=new StringBuffer("");
        File fileSource=new File(root.toString());
        BREAKER = 0;
        RecTree(fileSource);
        return tree.toString();
    }
    private void RecTree(File fileSource)
    {
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
    String SendStoreInfo() {
        return null;
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

    /**
     * Принимает относительный путь и путь для создания архива
     * @param sourceDir
     * @param zipFile
     * @throws IOException
     */
    public String Zip(String sourceDir, String zipFile) throws IOException {
        FileOutputStream fout=new FileOutputStream(relRoot+"\\"+zipFile);
        ZipOutputStream zout = new ZipOutputStream(fout);

        //шобы русские работали(не точно)
        zout.setEncoding("CP866");

        File fileSource = new File(relRoot,sourceDir);

        addDirectory(zout,fileSource);

        zout.close();
        return relRoot+SLASH+zipFile;
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

    private void createDir(final String dir)
    {
        File file = new File(dir);
        if (!file.exists())
            file.mkdirs();
    }
    private void createFolder(final String dirName)
    {
        if (dirName.endsWith(SLASH))
            createDir(dirName.substring(0, dirName.length() - 1));
    }
    private void checkFolder(final String file_path)
    {
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
}