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
			// Categories
			Category cat1 = new Category(null, "Electronics", "Electronic items", null);
			Category cat2 = new Category(null, "Furniture", "Home and office furniture", null);
			Category cat3 = new Category(null, "Groceries", "Daily groceries", null);
			Category cat4 = new Category(null, "Clothing", "Men and Women clothing", null);
			Category cat5 = new Category(null, "Office Supplies", "Stationery and office equipment", null);
			Category cat6 = new Category(null, "Tools", "Hardware and construction tools", null);
			categoryRepo.saveAll(List.of(cat1, cat2, cat3, cat4, cat5, cat6));

			// Providers
			Provider prov1 = new Provider(null, "TechCorp", "123456789", "New York", null, null);
			Provider prov2 = new Provider(null, "FurniPro", "987654321", "California", null, null);
			Provider prov3 = new Provider(null, "Foodies", "111222333", "Texas", null, null);
			Provider prov4 = new Provider(null, "Fashionista", "444555666", "Florida", null, null);
			Provider prov5 = new Provider(null, "OfficeMax", "777888999", "Chicago", null, null);
			Provider prov6 = new Provider(null, "ToolMaster", "555666777", "Detroit", null, null);
			providerRepo.saveAll(List.of(prov1, prov2, prov3, prov4, prov5, prov6));

			// Items - Electronics
			Item laptop = new Item(null, "Laptop", "Gaming laptop", "pcs", 10.0, "Electronics", 365, 5, 50, cat1, prov1, null, null, null, null, null);
			Item smartphone = new Item(null, "Smartphone", "Latest model smartphone", "pcs", 30.0, "Electronics", 365, 10, 100, cat1, prov1, null, null, null, null, null);
			Item tablet = new Item(null, "Tablet", "10-inch tablet", "pcs", 15.0, "Electronics", 365, 8, 60, cat1, prov1, null, null, null, null, null);
			Item headphones = new Item(null, "Headphones", "Wireless headphones", "pcs", 25.0, "Electronics", 365, 15, 80, cat1, prov1, null, null, null, null, null);

			// Items - Furniture
			Item chair = new Item(null, "Chair", "Office chair", "pcs", 20.0, "Furniture", 500, 10, 100, cat2, prov2, null, null, null, null, null);
			Item desk = new Item(null, "Desk", "Computer desk", "pcs", 15.0, "Furniture", 500, 5, 30, cat2, prov2, null, null, null, null, null);
			Item bookshelf = new Item(null, "Bookshelf", "Wooden bookshelf", "pcs", 10.0, "Furniture", 500, 3, 20, cat2, prov2, null, null, null, null, null);
			Item cabinet = new Item(null, "Cabinet", "File cabinet", "pcs", 8.0, "Furniture", 500, 4, 25, cat2, prov2, null, null, null, null, null);

			// Items - Groceries
			Item rice = new Item(null, "Rice", "Organic rice", "kg", 500.0, "Groceries", 180, 50, 1000, cat3, prov3, null, null, null, null, null);
			Item sugar = new Item(null, "Sugar", "White sugar", "kg", 300.0, "Groceries", 365, 30, 600, cat3, prov3, null, null, null, null, null);
			Item flour = new Item(null, "Flour", "All-purpose flour", "kg", 400.0, "Groceries", 180, 40, 800, cat3, prov3, null, null, null, null, null);
			Item oil = new Item(null, "Cooking Oil", "Vegetable oil", "liter", 200.0, "Groceries", 365, 20, 400, cat3, prov3, null, null, null, null, null);

			// Items - Clothing
			Item tshirt = new Item(null, "T-Shirt", "Cotton T-shirt", "pcs", 100.0, "Clothing", 730, 20, 500, cat4, prov4, null, null, null, null, null);
			Item jeans = new Item(null, "Jeans", "Denim jeans", "pcs", 80.0, "Clothing", 730, 15, 300, cat4, prov4, null, null, null, null, null);
			Item jacket = new Item(null, "Jacket", "Winter jacket", "pcs", 50.0, "Clothing", 730, 10, 200, cat4, prov4, null, null, null, null, null);
			Item socks = new Item(null, "Socks", "Cotton socks", "pairs", 150.0, "Clothing", 730, 30, 600, cat4, prov4, null, null, null, null, null);

			// Items - Office Supplies
			Item paper = new Item(null, "Paper", "A4 printing paper", "reams", 100.0, "Office Supplies", 730, 20, 500, cat5, prov5, null, null, null, null, null);
			Item pens = new Item(null, "Pens", "Ballpoint pens", "boxes", 50.0, "Office Supplies", 730, 10, 200, cat5, prov5, null, null, null, null, null);
			Item stapler = new Item(null, "Stapler", "Office stapler", "pcs", 30.0, "Office Supplies", 730, 5, 100, cat5, prov5, null, null, null, null, null);
			Item folders = new Item(null, "Folders", "File folders", "packs", 40.0, "Office Supplies", 730, 8, 150, cat5, prov5, null, null, null, null, null);

			// Items - Tools
			Item hammer = new Item(null, "Hammer", "Claw hammer", "pcs", 25.0, "Tools", 1825, 5, 50, cat6, prov6, null, null, null, null, null);
			Item screwdriver = new Item(null, "Screwdriver Set", "Multi-bit screwdriver set", "sets", 20.0, "Tools", 1825, 8, 80, cat6, prov6, null, null, null, null, null);
			Item wrench = new Item(null, "Wrench Set", "Adjustable wrench set", "sets", 15.0, "Tools", 1825, 6, 60, cat6, prov6, null, null, null, null, null);
			Item drill = new Item(null, "Power Drill", "Cordless power drill", "pcs", 10.0, "Tools", 1825, 4, 40, cat6, prov6, null, null, null, null, null);

			itemRepo.saveAll(List.of(
					laptop, smartphone, tablet, headphones,
					chair, desk, bookshelf, cabinet,
					rice, sugar, flour, oil,
					tshirt, jeans, jacket, socks,
					paper, pens, stapler, folders,
					hammer, screwdriver, wrench, drill
			));
			// Stored Locations - Section A (Electronics)
			StoredLocation locA1 = new StoredLocation(null, "A", "1", "1", "A101", false, false, 500, 0 ,laptop , null);
			StoredLocation locA2 = new StoredLocation(null, "A", "1", "2", "A102", false, false, 500, 0 ,laptop  , null);
			StoredLocation locA3 = new StoredLocation(null, "A", "2", "1", "A201", false, false, 500, 0 ,laptop  , null);
			StoredLocation locA4 = new StoredLocation(null, "A", "2", "2", "A202", false, false, 500, 0 ,laptop  , null);

			// Stored Locations - Section B (Furniture)
			StoredLocation locB1 = new StoredLocation(null, "B", "1", "1", "B101", false, false, 500, 0 ,laptop  , null);
			StoredLocation locB2 = new StoredLocation(null, "B", "1", "2", "B102", false, false, 500, 0 ,laptop  , null);
			StoredLocation locB3 = new StoredLocation(null, "B", "2", "1", "B201", false, false, 500, 0 ,laptop  , null);
			StoredLocation locB4 = new StoredLocation(null, "B", "2", "2", "B202", false, false, 500, 0 ,laptop  , null);

			// Stored Locations - Section C (Groceries)
			StoredLocation locC1 = new StoredLocation(null, "C", "1", "1", "C101", false, false, 500, 0 ,smartphone  , null);
			StoredLocation locC2 = new StoredLocation(null, "C", "1", "2", "C102", false, false, 500, 0 ,smartphone  , null);
			StoredLocation locC3 = new StoredLocation(null, "C", "2", "1", "C201", false, false, 500, 0 ,smartphone  , null);
			StoredLocation locC4 = new StoredLocation(null, "C", "2", "2", "C202", false, false, 500, 0 ,smartphone  , null);

			// Stored Locations - Section D (Clothing)
			StoredLocation locD1 = new StoredLocation(null, "D", "1", "1", "D101", false, false, 500, 0 ,smartphone , null);
			StoredLocation locD2 = new StoredLocation(null, "D", "1", "2", "D102", false, false, 500, 0 ,smartphone  , null);
			StoredLocation locD3 = new StoredLocation(null, "D", "2", "1", "D201", false, false, 500, 0 ,smartphone  , null);
			StoredLocation locD4 = new StoredLocation(null, "D", "2", "2", "D202", false, false, 500, 0 ,smartphone , null);

			// Stored Locations - Section E (Office Supplies)
			StoredLocation locE1 = new StoredLocation(null, "E", "1", "1", "E101", false, false, 500, 0 ,tablet  , null);
			StoredLocation locE2 = new StoredLocation(null, "E", "1", "2", "E102", false, false, 500, 0 ,tablet, null);
			StoredLocation locE3 = new StoredLocation(null, "E", "2", "1", "E201", false, false, 500, 0 ,tablet , null);
			StoredLocation locE4 = new StoredLocation(null, "E", "2", "2", "E202", false, false, 500, 0 ,tablet , null);

			// Stored Locations - Section F (Tools)
			StoredLocation locF1 = new StoredLocation(null, "F", "1", "1", "F101", false, false, 500, 0 ,tablet , null);
			StoredLocation locF2 = new StoredLocation(null, "F", "1", "2", "F102", false, false, 500, 0 ,tablet , null);
			StoredLocation locF3 = new StoredLocation(null, "F", "2", "1", "F201", false, false, 500, 0 ,tablet , null);
			StoredLocation locF4 = new StoredLocation(null, "F", "2", "2", "F202", false, false, 500, 0 ,tablet, null);

			storedLocationRepo.saveAll(List.of(
					locA1, locA2, locA3, locA4,
					locB1, locB2, locB3, locB4,
					locC1, locC2, locC3, locC4,
					locD1, locD2, locD3, locD4,
					locE1, locE2, locE3, locE4,
					locF1, locF2, locF3, locF4
			));

			Account acc1 = new Account(null, "admin@example.com", "12345", "123456789", "Admin User", "ACTIVE", true, false, AccountRole.ADMIN, null, null, null, null, null);
			Account acc2 = new Account(null, "warehousekeeper@example.com", "12345", "987654321", "User One", "ACTIVE", true, false, AccountRole.WAREHOUSE_KEEPER, null, null, null, null, null);
			Account acc3 = new Account(null, "warehousemanager@example.com", "12345", "111222333", "User Two", "ACTIVE", true, false, AccountRole.WAREHOUSE_MANAGER, null, null, null, null, null);
			Account acc4 = new Account(null, "department@example.com", "12345", "444555666", "Warehouse Manager", "ACTIVE", true, false, AccountRole.DEPARTMENT, null, null, null, null, null);
			accountRepo.saveAll(List.of(acc1, acc2, acc3, acc4));

			System.out.println("Created example values successfully");
		};
	}
}
