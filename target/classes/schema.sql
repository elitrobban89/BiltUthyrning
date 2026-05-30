-- Create Cars table
CREATE TABLE IF NOT EXISTS cars (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    model VARCHAR(255) NOT NULL,
    year VARCHAR(4) NOT NULL,
    engine VARCHAR(255) NOT NULL,
    daily_rate DECIMAL(10, 2) NOT NULL
);

-- Create Bookings table
CREATE TABLE IF NOT EXISTS bookings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    car_id INTEGER NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    FOREIGN KEY (car_id) REFERENCES cars(id)
);

-- Insert sample data (prices in SEK - Swedish Kronor)
INSERT OR IGNORE INTO cars (id, model, year, engine, daily_rate) VALUES
(1, 'Volvo XC40', '2023', '2.0L Turbo', 899.00),
(2, 'Volvo S90', '2023', '2.0L Turbo', 1299.00),
(3, 'Volvo V90', '2022', '2.0L B4', 1099.00),
(4, 'Volvo XC90', '2025', '1.5L B5', 1299.00);

