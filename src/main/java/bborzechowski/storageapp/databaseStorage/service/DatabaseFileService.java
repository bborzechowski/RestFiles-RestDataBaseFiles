package bborzechowski.storageapp.databaseStorage.service;

import bborzechowski.storageapp.databaseStorage.model.DataBaseFileManage;
import bborzechowski.storageapp.databaseStorage.model.DatabaseFile;
import bborzechowski.storageapp.databaseStorage.repository.DatabaseFileRepository;
import org.apache.logging.log4j.util.Strings;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DatabaseFileService {

    private DatabaseFileRepository databaseFileRepository;

    public DatabaseFileService(DatabaseFileRepository databaseFileRepository) {
        this.databaseFileRepository = databaseFileRepository;
    }

    public ResponseEntity<String> storeFile(MultipartFile file){

        String filename = StringUtils.cleanPath(file.getOriginalFilename());  //cleanPath(dla przykładu) bierze tylko nazwe pliku np plik.txt bez całej ściezki np C://dokplik.txt

        if(Strings.isBlank(filename)){
            return null;

        }
        long actualDate = new Date().getTime();
        Date date = new Date(actualDate);

        DatabaseFile databaseFile = new DatabaseFile();
        databaseFile.setFilename(filename);
        databaseFile.setFileType(file.getContentType());
        databaseFile.setDate(date);
        try {
            databaseFile.setData(file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        DatabaseFile result = databaseFileRepository.save(databaseFile);

        if(result == null){
            return new ResponseEntity<>("Cannot save file to data base", HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(file.getOriginalFilename(), HttpStatus.CREATED);
    }

    public List<DataBaseFileManage> getFiles(){

        List<DatabaseFile> files = databaseFileRepository.findAll();
        List<DataBaseFileManage> filesManage = new ArrayList<>();
        files.forEach(f -> {
                    String downloadUri = ServletUriComponentsBuilder
                            .fromCurrentContextPath() // dane o hoscie, porcie, protokole
                            .path("/api/v1/db/files/download/") //dane do endpointa
                            .path(f.getId())
                            .toUriString();

                    String deleteUri = ServletUriComponentsBuilder
                            .fromCurrentContextPath()
                            .path("/api/v1/db/files/delete/") //dane do endpointa
                            .path(f.getId())
                            .toUriString();

                    DataBaseFileManage dataBaseFileManage = new DataBaseFileManage();
                    dataBaseFileManage.setFilename(f.getFilename());
                    dataBaseFileManage.setDownloadUri(downloadUri);
                    dataBaseFileManage.setDeleteUri(deleteUri);
                    dataBaseFileManage.setFileType(f.getFileType());
                    dataBaseFileManage.setSize((long)f.getData().length);
                    filesManage.add(dataBaseFileManage);
                });
        return filesManage;
    }

    public ResponseEntity<String> storeFiles(MultipartFile[] files){

       List<ResponseEntity<String>> responses = Arrays.stream(files)
                .map(this::storeFile)
                .collect(Collectors.toList());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<Resource> downloadFile(String fileId){
        DatabaseFile databaseFile = databaseFileRepository.getOne(fileId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(databaseFile.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+ databaseFile.getFilename() + "\"" )  //bez tej linijki otworzy nam sie plik a nie sciagnie
                .header("Siema", "Pozdrowienia z bazy danych")
                .body(new ByteArrayResource(databaseFile.getData()));
    }

    public ResponseEntity<String> deleteFile(String fileId){
        databaseFileRepository.deleteById(fileId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
