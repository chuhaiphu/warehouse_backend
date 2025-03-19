package capstonesu25.warehouse.utils;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.experimental.UtilityClass;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
@Component
public class CloudinaryUtil {
    private final Cloudinary cloudinary;

    public CloudinaryUtil() {
        // Load .env file
        Dotenv dotenv = Dotenv.load();
        String cloudinaryUrl = dotenv.get("CLOUDINARY_URL");

        if (cloudinaryUrl == null || !cloudinaryUrl.startsWith("cloudinary://")) {
            throw new IllegalArgumentException("Invalid CLOUDINARY_URL scheme. Expecting to start with 'cloudinary://'");
        }

        this.cloudinary = new Cloudinary(cloudinaryUrl);
    }

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
