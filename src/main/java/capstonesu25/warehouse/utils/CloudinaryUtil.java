package capstonesu25.warehouse.utils;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Component
public class CloudinaryUtil {
    private final Cloudinary cloudinary;

    public CloudinaryUtil(@Value("${cloudinary.url}") String cloudinaryUrl) {
        if (cloudinaryUrl == null || !cloudinaryUrl.startsWith("cloudinary://")) {
            throw new IllegalArgumentException("Invalid CLOUDINARY_URL scheme. Expecting to start with 'cloudinary://'");
        }

        this.cloudinary = new Cloudinary(cloudinaryUrl);
    }

    @SuppressWarnings("unchecked")
    public String uploadImage(MultipartFile file) throws IOException {
        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "use_filename", false,
                "unique_filename", false,
                "overwrite", true
        );

        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
        return (String) uploadResult.get("url");
    }
}
