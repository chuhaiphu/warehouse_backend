package capstonesu25.warehouse;

import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.model.account.RegisterRequest;
import capstonesu25.warehouse.model.storedlocation.StoredLocationRequest;
import capstonesu25.warehouse.repository.*;
import capstonesu25.warehouse.service.AccountService;
import capstonesu25.warehouse.service.StoredLocationService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.InputStream;
import java.time.LocalTime;
import java.util.List;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableAspectJAutoProxy(exposeProxy = true)
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
			AccountService accountService,
			ConfigurationRepository configurationRepo,
			DepartmentRepository departmentRepo,
			StoredLocationService storedLocationService,
			ObjectMapper objectMapper) {
		return args -> {
			if (categoryRepo.count() > 0) {
				System.out.println("Data already exists, skipping initialization.");
				return;
			}
			if (itemRepo.count() > 0) {
				System.out.println("Data already exists, skipping initialization.");
				return;
			}
			if (providerRepo.count() > 0) {
				System.out.println("Data already exists, skipping initialization.");
				return;
			}
			if (storedLocationService.hasExistingData()) {
				System.out.println("Stored location data already exists, skipping initialization.");
				return;
			}
			if (accountRepo.count() > 0) {
				System.out.println("Data already exists, skipping initialization.");
				return;
			}
			// Configurations
			Configuration configuration = new Configuration(
					null,
					null,
					LocalTime.parse("07:00"),
					LocalTime.parse("17:00"),
					LocalTime.parse("04:00"),
					LocalTime.parse("02:00"),
					LocalTime.parse("01:00"),
					LocalTime.parse("23:00"),
					7,
					7,
					30,
					5,
					90);
			configurationRepo.save(configuration);

			// Categories
			Category cat1 = new Category(null, "Vải", "Các loại cuộn vải", null);
			Category cat2 = new Category(null, "Nút", "Các loại nút quần áo", null);
			Category cat3 = new Category(null, "Chỉ may", "Các loại chỉ may", null);
			Category cat4 = new Category(null, "Kim may", "Các loại kim may", null);
			Category cat5 = new Category(null, "Khóa kéo", "Các loại khóa kéo", null);
			Category cat6 = new Category(null, "Phụ liệu khác", "Các loại phụ liệu may mặc khác", null);
			categoryRepo.saveAll(List.of(cat1, cat2, cat3, cat4, cat5, cat6));

			// Providers
			Provider prov1 = new Provider(null, "Công ty Dệt may Phong Phú", "02838131767", "48 Tăng Nhơn Phú, TP.HCM",
					null, null);
			Provider prov2 = new Provider(null, "Công ty CP Dệt may Nam Định", "02283649555", "43 Tô Hiệu, Nam Định",
					null, null);
			Provider prov3 = new Provider(null, "Công ty TNHH Phụ liệu may Việt Nam", "02438245688",
					"Số 7 Lê Văn Lương, Hà Nội", null, null);
			Provider prov4 = new Provider(null, "Công ty TNHH Thành Công", "02838257699", "36 Tây Thạnh, TP.HCM", null,
					null);
			Provider prov5 = new Provider(null, "Công ty TNHH YKK Việt Nam", "0251383699", "KCN Biên Hòa, Đồng Nai",
					null, null);
			Provider prov6 = new Provider(null, "Công ty TNHH Phụ liệu may Phương Nam", "0283832145",
					"Q.Tân Bình, TP.HCM", null, null);
			providerRepo.saveAll(List.of(prov1, prov2, prov3, prov4, prov5, prov6));

			// Items từ Provider 1 - Công ty Dệt may Phong Phú và Provider 2
			Item vai1 = new Item("VAI-KT-001", "Cây vải Kate", "Vải kate 65/35", "mét", 0.0, 200.0, 0, "Cây", 730, 100,
					2000, 5, configuration, cat1, List.of(prov1, prov2), null, null, null, null, null);
			Item vai3 = new Item("VAI-KK-001", "Cây vải Kaki", "Vải kaki thun", "mét", 0.0, 200.0, 0, "Cây", 730, 150,
					2500, 5, configuration, cat1, List.of(prov1), null, null, null, null, null);

			// Items từ Provider 2 - Công ty CP Dệt may Nam Định và Provider 1
			Item vai2 = new Item("VAI-JE-001", "Cây vải Jean", "Vải jean 100% cotton", "mét", 0.0, 140.0, 0, "Cây", 730,
					200, 3000, 5, configuration, cat1, List.of(prov2, prov1), null, null, null, null, null);
			Item vai4 = new Item("VAI-TH-001", "Cây vải Thun", "Vải thun cotton 4 chiều", "mét", 0.0, 150.0, 0, "Cây",
					730, 100, 1500, 5, configuration, cat1, List.of(prov2), null, null, null, null, null);

			// Items từ Provider 3 - Công ty TNHH Phụ liệu may Việt Nam và Provider 6
			Item nut1 = new Item("NUT-NH-001", "Bịch nút nhựa 4 lỗ", "Nút nhựa màu trắng", "cái", 0.0, 300.0, 0, "Bịch",
					365, 20, 200, 5, configuration, cat2, List.of(prov3, prov6), null, null, null, null, null);
			Item nut2 = new Item("NUT-KL-001", "Bịch nút kim loại", "Nút jean kim loại", "cái", 0.0, 250.0, 0, "Bịch",
					365, 10, 100, 5, configuration, cat2, List.of(prov3), null, null, null, null, null);
			Item chi3 = new Item("CHI-JE-001", "Cuộn chỉ jean", "Chỉ may jean đặc biệt", "mét", 0.0, 300.0, 0, "Cuộn",
					730, 20, 200, 5, configuration, cat3, List.of(prov3, prov4), null, null, null, null, null);
			Item chi4 = new Item("CHI-TH-001", "Cuộn chỉ thêu", "Chỉ thêu đa màu", "mét", 0.0, 120.0, 0, "Cuộn", 730,
					15, 150, 5, configuration, cat3, List.of(prov3), null, null, null, null, null);
			Item kim1 = new Item("KIM-TH-001", "Hộp kim may thường", "Kim may size 90/14", "cái", 0.0, 200.0, 0, "Hộp",
					365, 10, 100, 5, configuration, cat4, List.of(prov3), null, null, null, null, null);
			Item kim2 = new Item("KIM-JE-001", "Hộp kim may jean", "Kim may jean size 100/16", "cái", 0.0, 200.0, 0,
					"Hộp", 365, 8, 80, 5, configuration, cat4, List.of(prov3, prov6), null, null, null, null, null);

			// Items từ Provider 4 - Công ty TNHH Thành Công và Provider 3
			Item chi1 = new Item("CHI-PL-001", "Cuộn chỉ polyester", "Chỉ may polyester 40/2", "mét", 0.0, 250.0, 0,
					"Cuộn", 730, 50, 500, 5, configuration, cat3, List.of(prov4, prov3), null, null, null, null, null);
			Item chi2 = new Item("CHI-CT-001", "Cuộn chỉ cotton", "Chỉ may cotton 100%", "mét", 0.0, 300.0, 0, "Cuộn",
					730, 30, 300, 5, configuration, cat3, List.of(prov4), null, null, null, null, null);

			// Items từ Provider 5 - Công ty TNHH YKK Việt Nam
			Item khoa1 = new Item("KHO-NH-001", "Bịch khóa kéo nhựa", "Khóa kéo nhựa 20cm", "cái", 0.0, 150.0, 0,
					"Bịch", 365, 20, 200, 5, configuration, cat5, List.of(prov5), null, null, null, null, null);
			Item khoa2 = new Item("KHO-KL-001", "Bịch khóa kéo kim loại", "Khóa kéo kim loại 15cm", "cái", 0.0, 100.0,
					0, "Bịch", 365, 15, 150, 5, configuration, cat5, List.of(prov5, prov6), null, null, null, null,
					null);
			Item khoa3 = new Item("KHO-JE-001", "Bịch khóa kéo jean", "Khóa kéo jean YKK", "cái", 0.0, 100.0, 0, "Bịch",
					365, 10, 100, 5, configuration, cat5, List.of(prov5), null, null, null, null, null);
			Item khoa4 = new Item("KHO-AK-001", "Bịch khóa kéo áo khoác", "Khóa kéo 2 chiều", "cái", 0.0, 350.0, 0,
					"Bịch", 365, 8, 80, 5, configuration, cat5, List.of(prov5), null, null, null, null, null);

			// Items từ Provider 6 - Công ty TNHH Phụ liệu may Phương Nam và Provider 2
			Item nut3 = new Item("NUT-VS-001", "Bịch nút áo vest", "Nút áo vest cao cấp", "cái", 0.0, 300.0, 0, "Bịch",
					365, 5, 50, 5, configuration, cat2, List.of(prov6, prov2), null, null, null, null, null);
			Item nut4 = new Item("NUT-GO-001", "Bịch nút gỗ", "Nút gỗ tự nhiên", "cái", 0.0, 250.0, 0, "Bịch", 365, 8,
					80, 5, configuration, cat2, List.of(prov6), null, null, null, null, null);
			Item kim3 = new Item("KIM-DA-001", "Hộp kim may da", "Kim may da đặc biệt", "cái", 0.0, 250.0, 0, "Hộp",
					365, 5, 50, 5, configuration, cat4, List.of(prov6, prov3), null, null, null, null, null);
			Item kim4 = new Item("KIM-TH-002", "Hộp kim thêu", "Kim thêu các loại", "cái", 0.0, 250.0, 0, "Hộp", 365, 5,
					40, 5, configuration, cat4, List.of(prov6), null, null, null, null, null);

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
					nut3, nut4, kim3, kim4));

			// Set provider.items for all providers
			prov1.setItems(List.of(vai1, vai3, vai2));
			prov2.setItems(List.of(vai2, vai4, vai1, nut3));
			prov3.setItems(List.of(nut1, nut2, chi3, chi4, kim1, kim2, chi1, kim3));
			prov4.setItems(List.of(chi1, chi2, chi3));
			prov5.setItems(List.of(khoa1, khoa2, khoa3, khoa4));
			prov6.setItems(List.of(nut3, nut4, kim3, kim4, nut1, khoa2, kim2));

			// Set item.providers for all items
			vai1.setProviders(List.of(prov1, prov2));
			vai3.setProviders(List.of(prov1));
			vai2.setProviders(List.of(prov2, prov1));
			vai4.setProviders(List.of(prov2));

			nut1.setProviders(List.of(prov3, prov6));
			nut2.setProviders(List.of(prov3));
			chi3.setProviders(List.of(prov3, prov4));
			chi4.setProviders(List.of(prov3));
			kim1.setProviders(List.of(prov3));
			kim2.setProviders(List.of(prov3, prov6));

			chi1.setProviders(List.of(prov4, prov3));
			chi2.setProviders(List.of(prov4));

			khoa1.setProviders(List.of(prov5));
			khoa2.setProviders(List.of(prov5, prov6));
			khoa3.setProviders(List.of(prov5));
			khoa4.setProviders(List.of(prov5));

			nut3.setProviders(List.of(prov6, prov2));
			nut4.setProviders(List.of(prov6));
			kim3.setProviders(List.of(prov6, prov3));
			kim4.setProviders(List.of(prov6));

			// Save all providers with relationships (owning side)
			providerRepo.saveAll(List.of(prov1, prov2, prov3, prov4, prov5, prov6));

			ClassPathResource resource = new ClassPathResource("data/warehouse_location.json");

			try (InputStream inputStream = resource.getInputStream()) {
				List<StoredLocationRequest> locationRequests = objectMapper.readValue(
						inputStream,
						new TypeReference<List<StoredLocationRequest>>() {
						});

				storedLocationService.create(locationRequests);
			}

			// Initialize Departments
			Department department1 = new Department(
					null,
					"Phòng ban A",
					"Nguyễn Văn A",
					"123 Đường Số 1, Quận 1, TP.HCM",
					"0901234567");

			Department department2 = new Department(
					null,
					"Phòng ban B",
					"Trần Thị B",
					"456 Đường Số 2, Quận 2, TP.HCM",
					"0912345678");
			Department department3 = new Department(
					null,
					"Phòng ban C",
					"Lê Văn C",
					"789 Đường Số 3, Quận 3, TP.HCM",
					"0923456789");

			Department department4 = new Department(
					null,
					"Phòng ban D",
					"Phạm Thị D",
					"101 Đường Số 4, Quận 4, TP.HCM",
					"0934567890");

			Department department5 = new Department(
					null,
					"Phòng ban E",
					"Hoàng Văn E",
					"202 Đường Số 5, Quận 5, TP.HCM",
					"0945678901");

			Department department6 = new Department(
					null,
					"Phòng ban F",
					"Đặng Thị F",
					"303 Đường Số 6, Quận 6, TP.HCM",
					"0956789012");

			Department department7 = new Department(
					null,
					"Phòng ban G",
					"Ngô Văn G",
					"404 Đường Số 7, Quận 7, TP.HCM",
					"0967890123");

			Department department8 = new Department(
					null,
					"Phòng ban H",
					"Vũ Thị H",
					"505 Đường Số 8, Quận 8, TP.HCM",
					"0978901234");

			Department department9 = new Department(
					null,
					"Phòng ban I",
					"Bùi Văn I",
					"606 Đường Số 9, Quận 9, TP.HCM",
					"0989012345");

			Department department10 = new Department(
					null,
					"Phòng ban J",
					"Đỗ Thị J",
					"707 Đường Số 10, Quận 10, TP.HCM",
					"0990123456");

			Department department11 = new Department(
					null,
					"Phòng ban K",
					"Trịnh Văn K",
					"808 Đường Số 11, Quận 11, TP.HCM",
					"0901122334");

			Department department12 = new Department(
					null,
					"Phòng ban L",
					"Mai Thị L",
					"909 Đường Số 12, Quận 12, TP.HCM",
					"0911223344");

			departmentRepo.saveAll(List.of(
					department1, department2, department3, department4,
					department5, department6, department7, department8,
					department9, department10, department11, department12));

			// Initialize Accounts
			RegisterRequest admin = RegisterRequest.builder()
					.username("admin")
					.email("admin@warehouse.com")
					.password("12345")
					.phone("0901234567")
					.fullName("Nguyễn Văn Admin")
					.role("ADMIN")
					.build();
			accountService.register(admin);

			RegisterRequest warehouseManager = RegisterRequest.builder()
					.username("quanly")
					.email("quanly@warehouse.com")
					.password("12345")
					.phone("0912345678")
					.fullName("Trần Thị Quản Lý")
					.role("WAREHOUSE_MANAGER")
					.build();
			accountService.register(warehouseManager);

			RegisterRequest department = RegisterRequest.builder()
					.username("phongban")
					.email("phongban@warehouse.com")
					.password("12345")
					.phone("0923456789")
					.fullName("Lê Văn Phòng")
					.role("DEPARTMENT")
					.build();
			accountService.register(department);

			RegisterRequest staff1 = RegisterRequest.builder()
					.username("nhanvien1")
					.email("nhanvien1@warehouse.com")
					.password("12345")
					.phone("0934567890")
					.fullName("Phạm Thị Nhân")
					.role("STAFF")
					.build();
			accountService.register(staff1);

			RegisterRequest staff2 = RegisterRequest.builder()
					.username("nhanvien2")
					.email("nhanvien2@warehouse.com")
					.password("12345")
					.phone("0945678901")
					.fullName("Hoàng Văn Viên")
					.role("STAFF")
					.build();
			accountService.register(staff2);

			RegisterRequest staff3 = RegisterRequest.builder()
					.username("nhanvien3")
					.email("nhanvien3@warehouse.com")
					.password("12345")
					.phone("0956789012")
					.fullName("Trần Văn Vũ")
					.role("STAFF")
					.build();
			accountService.register(staff3);

			RegisterRequest staff4 = RegisterRequest.builder()
					.username("nhanvien4")
					.email("nhanvien4@warehouse.com")
					.password("12345")
					.phone("0967890123")
					.fullName("Nguyễn Thị Hà")
					.role("STAFF")
					.build();
			accountService.register(staff4);

			RegisterRequest staff5 = RegisterRequest.builder()
					.username("nhanvien5")
					.email("nhanvien5@warehouse.com")
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
