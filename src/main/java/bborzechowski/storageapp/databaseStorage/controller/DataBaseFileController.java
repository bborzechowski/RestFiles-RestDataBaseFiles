package bborzechowski.storageapp.databaseStorage.controller;

import bborzechowski.storageapp.databaseStorage.model.DataBaseFileManage;
import bborzechowski.storageapp.databaseStorage.service.DatabaseFileService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/db")
public class DataBaseFileController {

    private DatabaseFileService databaseFileService;

    public DataBaseFileController(DatabaseFileService databaseFileService) {
        this.databaseFileService = databaseFileService;
    }

    @PostMapping("/files")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file){
       return databaseFileService.storeFile(file);
    }

    @PostMapping("/files/many")
    public ResponseEntity<String> uploadFiles(@RequestParam("files") MultipartFile[] files){
        return databaseFileService.storeFiles(files);
    }

    @GetMapping("/files")
    public ResponseEntity<List<DataBaseFileManage>> getFiles(){
        return  new ResponseEntity<>(databaseFileService.getFiles(), HttpStatus.OK);
    }

    @GetMapping("files/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId){
       return databaseFileService.downloadFile(fileId);
    }

    @DeleteMapping("files/delete/{fileId}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileId){
        return databaseFileService.deleteFile(fileId);
    }
}
