package capstonesu25.warehouse;

import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.enums.AccountRole;
import capstonesu25.warehouse.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
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
			AccountRepository accountRepo
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
			Category cat1 = new Category(null, "Electronics", "Electronic items", null);
			Category cat2 = new Category(null, "Furniture", "Home and office furniture", null);
			Category cat3 = new Category(null, "Groceries", "Daily groceries", null);
			Category cat4 = new Category(null, "Clothing", "Men and Women clothing", null);
			categoryRepo.saveAll(List.of(cat1, cat2, cat3, cat4));

			Provider prov1 = new Provider(null, "TechCorp", "123456789", "New York", null, null);
			Provider prov2 = new Provider(null, "FurniPro", "987654321", "California", null, null);
			Provider prov3 = new Provider(null, "Foodies", "111222333", "Texas", null, null);
			Provider prov4 = new Provider(null, "Fashionista", "444555666", "Florida", null, null);
			providerRepo.saveAll(List.of(prov1, prov2, prov3, prov4));

			Item item1 = new Item(null, "Laptop", "Gaming laptop", "pcs", 10.0, "Electronics", 365, 5, 50, cat1, prov1, null, null, null, null);
			Item item2 = new Item(null, "Chair", "Office chair", "pcs", 20.0, "Furniture", 500, 10, 100, cat1, prov2, null, null, null, null);
			Item item3 = new Item(null, "Rice", "Organic rice", "kg", 500.0, "Groceries", 180, 50, 1000, cat2, prov2, null, null, null, null);
			Item item4 = new Item(null, "T-Shirt", "Cotton T-shirt", "pcs", 100.0, "Clothing", 730, 20, 500, cat2, prov2, null, null, null, null);
			itemRepo.saveAll(List.of(item1, item2, item3, item4));

			StoredLocation loc1 = new StoredLocation(null, "A", "1", "1", "101", false, false, null);
			StoredLocation loc2 = new StoredLocation(null, "B", "2", "3", "202", false, false, null);
			StoredLocation loc3 = new StoredLocation(null, "C", "1", "2", "303", false, false, null);
			StoredLocation loc4 = new StoredLocation(null, "D", "3", "4", "404", false, false, null);
			storedLocationRepo.saveAll(List.of(loc1, loc2, loc3, loc4));

			Account acc1 = new Account(null, "admin@example.com", "12345", "123456789", "Admin User", "ACTIVE", true, false, AccountRole.ADMIN, null, null, null, null, null);
			Account acc2 = new Account(null, "warehousekeeper@example.com", "12345", "987654321", "User One", "ACTIVE", true, false, AccountRole.WAREHOUSE_KEEPER, null, null, null, null, null);
			Account acc3 = new Account(null, "warehousemanager@example.com", "12345", "111222333", "User Two", "ACTIVE", true, false, AccountRole.WAREHOUSE_MANAGER, null, null, null, null, null);
			Account acc4 = new Account(null, "department@example.com", "12345", "444555666", "Warehouse Manager", "ACTIVE", true, false, AccountRole.DEPARTMENT, null, null, null, null, null);
			accountRepo.saveAll(List.of(acc1, acc2, acc3, acc4));

			System.out.println("create example value success");
		};
	}

}
