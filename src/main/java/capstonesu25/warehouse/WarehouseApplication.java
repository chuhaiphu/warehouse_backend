package capstonesu25.warehouse;

import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.model.account.RegisterRequest;
import capstonesu25.warehouse.repository.*;
import capstonesu25.warehouse.service.AccountService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.List;

@SpringBootApplication
@EnableAsync
public class WarehouseApplication {

	public static void main(String[] args) {
		SpringApplication.run(WarehouseApplication.class, args);
	}

	@Bean
	CommandLineRunner initData(
			CategoryRepository categoryRepo,
			ItemRepository itemRepo,
			ProviderRepository providerRepo,
			StoredLocationRepository storedLocationRepo,
			AccountRepository accountRepo,
			AccountService accountService
	) {
		return args -> {
			if (categoryRepo.count() > 0) {
				System.out.println("Data already exists, skipping initialization.");
				return;
			}
			if(itemRepo.count() > 0) {
				System.out.println("Data already exists, skipping initialization.");
				return;
			}
			if(providerRepo.count() > 0) {
				System.out.println("Data already exists, skipping initialization.");
				return;
			}
			if(storedLocationRepo.count() > 0) {
				System.out.println("Data already exists, skipping initialization.");
				return;
			}
			if(accountRepo.count() > 0) {
				System.out.println("Data already exists, skipping initialization.");
				return;
			}
			// Categories
			Category cat1 = new Category(null, "Vải", "Các loại cuộn vải", null);
			Category cat2 = new Category(null, "Nút", "Các loại nút quần áo", null);
			Category cat3 = new Category(null, "Chỉ may", "Các loại chỉ may", null);
			Category cat4 = new Category(null, "Kim may", "Các loại kim may", null);
			Category cat5 = new Category(null, "Khóa kéo", "Các loại khóa kéo", null);
			Category cat6 = new Category(null, "Phụ liệu khác", "Các loại phụ liệu may mặc khác", null);
			categoryRepo.saveAll(List.of(cat1, cat2, cat3, cat4, cat5, cat6));

			// Providers
			Provider prov1 = new Provider(null, "Công ty Dệt may Phong Phú", "02838131767", "48 Tăng Nhơn Phú, TP.HCM", null, null);
			Provider prov2 = new Provider(null, "Công ty CP Dệt may Nam Định", "02283649555", "43 Tô Hiệu, Nam Định", null, null);
			Provider prov3 = new Provider(null, "Công ty TNHH Phụ liệu may Việt Nam", "02438245688", "Số 7 Lê Văn Lương, Hà Nội", null, null);
			Provider prov4 = new Provider(null, "Công ty TNHH Thành Công", "02838257699", "36 Tây Thạnh, TP.HCM", null, null);
			Provider prov5 = new Provider(null, "Công ty TNHH YKK Việt Nam", "0251383699", "KCN Biên Hòa, Đồng Nai", null, null);
			Provider prov6 = new Provider(null, "Công ty TNHH Phụ liệu may Phương Nam", "0283832145", "Q.Tân Bình, TP.HCM", null, null);
			providerRepo.saveAll(List.of(prov1, prov2, prov3, prov4, prov5, prov6));

			// Items từ Provider 1 - Công ty Dệt may Phong Phú
			Item vai1 = new Item(null, "Vải Kate", "Vải kate 65/35", "mét", 1500.0, 1.0, 1500, "Vải", 730, 100, 2000, cat1, prov1, null, null, null, null, null);
			Item vai3 = new Item(null, "Vải Kaki", "Vải kaki thun", "mét", 1000.0, 1.0, 1000, "Vải", 730, 150, 2500, cat1, prov1, null, null, null, null, null);

			// Items từ Provider 2 - Công ty CP Dệt may Nam Định
			Item vai2 = new Item(null, "Vải Jean", "Vải jean 100% cotton", "mét", 2000.0, 1.0, 2000, "Vải", 730, 200, 3000, cat1, prov2, null, null, null, null, null);
			Item vai4 = new Item(null, "Vải Thun", "Vải thun cotton 4 chiều", "mét", 800.0, 1.0, 800, "Vải", 730, 100, 1500, cat1, prov2, null, null, null, null, null);

			// Items từ Provider 3 - Công ty TNHH Phụ liệu may Việt Nam
			Item nut1 = new Item(null, "Nút nhựa 4 lỗ", "Nút nhựa màu trắng", "bịch", 100.0, 1.0, 100, "Nút", 365, 20, 200, cat2, prov3, null, null, null, null, null);
			Item nut2 = new Item(null, "Nút kim loại", "Nút jean kim loại", "bịch", 50.0, 1.0, 50, "Nút", 365, 10, 100, cat2, prov3, null, null, null, null, null);
			Item chi3 = new Item(null, "Chỉ jean", "Chỉ may jean đặc biệt", "cuộn", 100.0, 1.0, 100, "Chỉ", 730, 20, 200, cat3, prov3, null, null, null, null, null);
			Item chi4 = new Item(null, "Chỉ thêu", "Chỉ thêu đa màu", "cuộn", 80.0, 1.0, 80, "Chỉ", 730, 15, 150, cat3, prov3, null, null, null, null, null);
			Item kim1 = new Item(null, "Kim may thường", "Kim may size 90/14", "hộp", 50.0, 1.0, 50, "Kim", 365, 10, 100, cat4, prov3, null, null, null, null, null);
			Item kim2 = new Item(null, "Kim may jean", "Kim may jean size 100/16", "hộp", 40.0, 1.0, 40, "Kim", 365, 8, 80, cat4, prov3, null, null, null, null, null);

			// Items từ Provider 4 - Công ty TNHH Thành Công
			Item chi1 = new Item(null, "Chỉ polyester", "Chỉ may polyester 40/2", "cuộn", 200.0, 1.0, 200, "Chỉ", 730, 50, 500, cat3, prov4, null, null, null, null, null);
			Item chi2 = new Item(null, "Chỉ cotton", "Chỉ may cotton 100%", "cuộn", 150.0, 1.0, 150, "Chỉ", 730, 30, 300, cat3, prov4, null, null, null, null, null);

			// Items từ Provider 5 - Công ty TNHH YKK Việt Nam
			Item khoa1 = new Item(null, "Khóa kéo nhựa", "Khóa kéo nhựa 20cm", "bịch", 100.0, 1.0, 100, "Khóa", 365, 20, 200, cat5, prov5, null, null, null, null, null);
			Item khoa2 = new Item(null, "Khóa kéo kim loại", "Khóa kéo kim loại 15cm", "bịch", 80.0, 1.0, 80, "Khóa", 365, 15, 150, cat5, prov5, null, null, null, null, null);
			Item khoa3 = new Item(null, "Khóa kéo jean", "Khóa kéo jean YKK", "bịch", 60.0, 1.0, 60, "Khóa", 365, 10, 100, cat5, prov5, null, null, null, null, null);
			Item khoa4 = new Item(null, "Khóa kéo áo khoác", "Khóa kéo 2 chiều", "bịch", 40.0, 1.0, 40, "Khóa", 365, 8, 80, cat5, prov5, null, null, null, null, null);

			// Items từ Provider 6 - Công ty TNHH Phụ liệu may Phương Nam
			Item nut3 = new Item(null, "Nút áo vest", "Nút áo vest cao cấp", "bịch", 30.0, 1.0, 30, "Nút", 365, 5, 50, cat2, prov6, null, null, null, null, null);
			Item nut4 = new Item(null, "Nút gỗ", "Nút gỗ tự nhiên", "bịch", 40.0, 1.0, 40, "Nút", 365, 8, 80, cat2, prov6, null, null, null, null, null);
			Item kim3 = new Item(null, "Kim may da", "Kim may da đặc biệt", "hộp", 30.0, 1.0, 30, "Kim", 365, 5, 50, cat4, prov6, null, null, null, null, null);
			Item kim4 = new Item(null, "Kim thêu", "Kim thêu các loại", "hộp", 25.0, 1.0, 25, "Kim", 365, 5, 40, cat4, prov6, null, null, null, null, null);

			// Lưu tất cả các items
			itemRepo.saveAll(List.of(
					// Provider 1 - Công ty Dệt may Phong Phú
					vai1, vai3,
					
					// Provider 2 - Công ty CP Dệt may Nam Định
					vai2, vai4,
					
					// Provider 3 - Công ty TNHH Phụ liệu may Việt Nam
					nut1, nut2, chi3, chi4, kim1, kim2,
					
					// Provider 4 - Công ty TNHH Thành Công
					chi1, chi2,
					
					// Provider 5 - Công ty TNHH YKK Việt Nam
					khoa1, khoa2, khoa3, khoa4,
					
					// Provider 6 - Công ty TNHH Phụ liệu may Phương Nam
					nut3, nut4, kim3, kim4
			));

			// Stored Locations - Section A (Electronics)
			StoredLocation locA1 = new StoredLocation(null, "A", "1", "1", "A101", false, false, 500, 0 ,vai1 , null);
			StoredLocation locA2 = new StoredLocation(null, "A", "1", "2", "A102", false, false, 500, 0 ,vai2  , null);
			StoredLocation locA3 = new StoredLocation(null, "A", "2", "1", "A201", false, false, 500, 0 ,vai3  , null);
			StoredLocation locA4 = new StoredLocation(null, "A", "2", "2", "A202", false, false, 500, 0 ,vai4  , null);

			// Stored Locations - Section B (Furniture)
			StoredLocation locB1 = new StoredLocation(null, "B", "1", "1", "B101", false, false, 500, 0 ,nut1  , null);
			StoredLocation locB2 = new StoredLocation(null, "B", "1", "2", "B102", false, false, 500, 0 ,nut2  , null);
			StoredLocation locB3 = new StoredLocation(null, "B", "2", "1", "B201", false, false, 500, 0 ,nut3  , null);
			StoredLocation locB4 = new StoredLocation(null, "B", "2", "2", "B202", false, false, 500, 0 ,nut4  , null);

			// Stored Locations - Section C (Groceries)
			StoredLocation locC1 = new StoredLocation(null, "C", "1", "1", "C101", false, false, 500, 0 ,chi1  , null);
			StoredLocation locC2 = new StoredLocation(null, "C", "1", "2", "C102", false, false, 500, 0 ,chi2  , null);
			StoredLocation locC3 = new StoredLocation(null, "C", "2", "1", "C201", false, false, 500, 0 ,chi3  , null);
			StoredLocation locC4 = new StoredLocation(null, "C", "2", "2", "C202", false, false, 500, 0 ,chi4  , null);

			// Stored Locations - Section D (Clothing)
			StoredLocation locD1 = new StoredLocation(null, "D", "1", "1", "D101", false, false, 500, 0 ,kim1 , null);
			StoredLocation locD2 = new StoredLocation(null, "D", "1", "2", "D102", false, false, 500, 0 ,kim2  , null);
			StoredLocation locD3 = new StoredLocation(null, "D", "2", "1", "D201", false, false, 500, 0 ,kim3  , null);
			StoredLocation locD4 = new StoredLocation(null, "D", "2", "2", "D202", false, false, 500, 0 ,kim4 , null);

			// Stored Locations - Section E (Office Supplies)
			StoredLocation locE1 = new StoredLocation(null, "E", "1", "1", "E101", false, false, 500, 0 ,khoa1  , null);
			StoredLocation locE2 = new StoredLocation(null, "E", "1", "2", "E102", false, false, 500, 0 ,khoa2, null);
			StoredLocation locE3 = new StoredLocation(null, "E", "2", "1", "E201", false, false, 500, 0 ,khoa3 , null);
			StoredLocation locE4 = new StoredLocation(null, "E", "2", "2", "E202", false, false, 500, 0 ,khoa4 , null);

			// Stored Locations - Section F (Tools)
			StoredLocation locF1 = new StoredLocation(null, "F", "1", "1", "F101", false, false, 500, 0 ,khoa1  , null);
			StoredLocation locF2 = new StoredLocation(null, "F", "1", "2", "F102", false, false, 500, 0 ,khoa2 , null);
			StoredLocation locF3 = new StoredLocation(null, "F", "2", "1", "F201", false, false, 500, 0 ,khoa3 , null);
			StoredLocation locF4 = new StoredLocation(null, "F", "2", "2", "F202", false, false, 500, 0 ,khoa4, null);

			storedLocationRepo.saveAll(List.of(
					locA1, locA2, locA3, locA4,
					locB1, locB2, locB3, locB4,
					locC1, locC2, locC3, locC4,
					locD1, locD2, locD3, locD4,
					locE1, locE2, locE3, locE4,
					locF1, locF2, locF3, locF4
			));

			// Replace the direct account creation with RegisterRequest
			RegisterRequest admin = RegisterRequest.builder()
				.email("admin@gmail.com")
				.password("12345")
				.phone("0901234567")
				.fullName("Nguyễn Văn Admin")
				.role("ADMIN")
				.build();
			accountService.register(admin);

			RegisterRequest warehouseManager = RegisterRequest.builder()
				.email("quanly@gmail.com")
				.password("12345")
				.phone("0912345678")
				.fullName("Trần Thị Quản Lý")
				.role("WAREHOUSE_MANAGER")
				.build();
			accountService.register(warehouseManager);

			RegisterRequest department = RegisterRequest.builder()
				.email("phongban@gmail.com")
				.password("12345")
				.phone("0923456789")
				.fullName("Lê Văn Phòng")
				.role("DEPARTMENT")
				.build();
			accountService.register(department);

			RegisterRequest staff1 = RegisterRequest.builder()
				.email("nhanvien1@gmail.com")
				.password("12345")
				.phone("0934567890")
				.fullName("Phạm Thị Nhân")
				.role("STAFF")
				.build();
			accountService.register(staff1);
			
			RegisterRequest staff2 = RegisterRequest.builder()
				.email("nhanvien2@gmail.com")
				.password("12345")
				.phone("0945678901")
				.fullName("Hoàng Văn Viên")
				.role("STAFF")
				.build();
			accountService.register(staff2);

			RegisterRequest staff3 = RegisterRequest.builder()
				.email("nhanvien3@gmail.com")
				.password("12345")
				.phone("0956789012")
				.fullName("Trần Văn Vũ")
				.role("STAFF")
				.build();
			accountService.register(staff3);

			RegisterRequest staff4 = RegisterRequest.builder()
				.email("nhanvien4@gmail.com")
				.password("12345")
				.phone("0967890123")
				.fullName("Nguyễn Thị Hà")
				.role("STAFF")
				.build(); 
			accountService.register(staff4);

			RegisterRequest staff5 = RegisterRequest.builder()
				.email("nhanvien5@gmail.com")
				.password("12345")
				.phone("0978901234")
				.fullName("Lê Văn Đại")
				.role("STAFF")
				.build();
			accountService.register(staff5);

			System.out.println("Created example values successfully");
		};
	}
}
