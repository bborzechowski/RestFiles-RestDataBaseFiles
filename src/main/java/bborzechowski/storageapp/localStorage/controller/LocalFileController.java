package bborzechowski.storageapp.localStorage.controller;

import bborzechowski.storageapp.localStorage.model.LocalFileManage;
import bborzechowski.storageapp.localStorage.service.LocalFileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin //serwis otwarty lub, {porty jakie moga miec dostep do aplikacji}
@RequestMapping("/api/v1/")
public class LocalFileController {

    private LocalFileService localFileService;

    public LocalFileController(LocalFileService localFileService) {
        this.localFileService = localFileService;
    }

    @GetMapping("/files")
    private List<LocalFileManage> getFiles(){
        return localFileService.getFiles();
    }

    @GetMapping("/files/download/{filename}")
    private ResponseEntity<?> downloadFile(@PathVariable String filename){
        return localFileService.getFile(filename);
    }

    @PostMapping("/files")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file){
       return localFileService.uploadFile(file);
    }

    @DeleteMapping("files/delete/{filename}")
    private ResponseEntity<String> deleteFile(@PathVariable String filename){
       return localFileService.deleteFile(filename);
    }
}
