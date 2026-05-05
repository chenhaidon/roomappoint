package com.example.web.controller;

import com.example.web.tools.dto.FileResultDto;
import com.example.web.tools.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;

/**
 * 文件上传接口
 */
@RestController()
@RequestMapping("/File")
public class FileController {

    /**
     * 批量文件上传
     * @param files 上传的文件
     * @return
     */
    @PostMapping("/BatchUpload")
    public ArrayList<FileResultDto> uploadFile(@RequestParam("file") MultipartFile[] files, HttpServletRequest request) {
        //定义一个存储文件的列表
        ArrayList<FileResultDto> fileResultDtos = new ArrayList<>();

        //循环处理一下文件 是否满足格式,不满足直接报错给前端
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                throw new CustomException("文件不能为空");
            }
            if (file.getSize() <= 0) {
                throw new CustomException("上传的文件不能为空!请重新上传");
            }
        }

        Path baseDir = Path.of(System.getProperty("user.dir"), "uploads").toAbsolutePath().normalize();

        //循环保存文件到 uploads 路径下
        for (MultipartFile file : files) {
            //获取文件原始的名称
            String originFileName = file.getOriginalFilename();
            //随机生成一个时间
            Long time = new Date().getTime();

            //声明一个保存目录的路径
            Path dirPath = baseDir.resolve(String.valueOf(time));

            //创建一个文件或者文件夹的操作对象
            File dirFile = dirPath.toFile();

            //判断文件是否存在 不存在的话执行下面的代码
            if (!dirFile.exists()) {
                //创建这个目录
                dirFile.mkdirs();
            }

            //try catch处理流  防止报错导致系统崩掉
            try (FileOutputStream fileOutputStream = new FileOutputStream(dirPath.resolve(originFileName).toFile())) {
                //把前端传入的内容以byte是格式写入到流里面
                fileOutputStream.write(file.getBytes());
                //结束流
                fileOutputStream.flush();

                //返回COS完整URL
                String url = "https://chd-1314768655.cos-website.ap-shanghai.myqcloud.com/uploads/" + time + "/" + originFileName;
                //加入到返回的列表中
                fileResultDtos.add(new FileResultDto(url, originFileName));

            } catch (java.io.IOException e) {
                e.printStackTrace();
            }

        }

        return fileResultDtos;
    }
}
