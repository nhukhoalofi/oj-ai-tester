# Hướng dẫn cài đặt

Tài liệu này mô tả cách thiết lập môi trường để chạy project **OJ AI Tester** trên máy Windows / Linux / macOS.

## 1. Yêu cầu hệ thống

- **Java 17**
- **Maven 3.x**
- **SQL Server** đang chạy local
- **Microsoft SQL Server JDBC Driver**: đã được khai báo trong `pom.xml`
- **g++ / MinGW-w64** để compile và chạy submission C++
- **Gemini API key** để dùng các chức năng AI
- Nếu dùng import từ ảnh, nên cài thêm **Tesseract OCR** và cập nhật đúng đường dẫn trong cấu hình nếu môi trường yêu cầu

## 2. Chuẩn bị database

Project đang dùng database có tên:

- `OJ_AI_TESTER`

File khởi tạo schema nằm tại:

- `scripts/schema.sql`

Script này tạo các bảng chính:

- `problems`
- `parsed_problems`
- `test_cases`
- `submissions`
- `execution_results`

Nếu bạn đang dùng một database cũ, có thể cần chạy thêm các file `scripts/alter_*.sql` để đồng bộ schema.

### Tạo database và bảng

1. Mở SQL Server Management Studio hoặc công cụ SQL tương đương.
2. Chạy file `scripts/schema.sql`.
3. Kiểm tra database `OJ_AI_TESTER` đã được tạo thành công.

## 3. Cấu hình ứng dụng

File cấu hình chính:

- `src/main/resources/application.properties`

Các mục quan trọng:

- `db.url` trỏ tới SQL Server local
- `ai.apiKey` hoặc biến môi trường `GEMINI_API_KEY`
- `ai.model`
- `ocr.tesseractPath` nếu dùng OCR
- `execution.timeLimitMs`

### Lưu ý về đăng nhập database

Project hiện được cấu hình theo hướng **Windows Authentication** với:

- `integratedSecurity=true`

Nếu máy bạn không dùng Windows Authentication, cần chỉnh lại chuỗi kết nối cho phù hợp với môi trường của bạn.

## 4. Cài đặt g++ / MinGW-w64

Chức năng chạy submission C++ cần `g++` có trong PATH.

### Kiểm tra nhanh trên Windows

```bat
g++ --version
```

Nếu chưa có, hãy cài MinGW-w64 và thêm thư mục `bin` vào biến môi trường `PATH`.

## 5. Chạy script setup

Project có sẵn 2 script hỗ trợ:

- `setup.bat` cho Windows
- `setup.sh` cho Linux / macOS

Các script này sẽ:

- kiểm tra Java, Maven, g++
- tạo thư mục `submissions/testcases`
- build project bằng Maven

### Windows

```bat
setup.bat
```

### Linux / macOS

```bash
chmod +x setup.sh
./setup.sh
```

## 6. Build project thủ công

Nếu muốn build bằng tay:

```bash
mvn clean package -DskipTests
```

## 7. Chạy ứng dụng

```bash
mvn javafx:run
```

Hoặc chạy class chính `com.yourteam.ojaitester.MainApp` / `AppLauncher` từ IDE.

## 8. Kiểm tra sau khi chạy

Sau khi mở ứng dụng, nên kiểm tra các điểm sau:

- Kết nối database thành công
- Danh sách problem load được từ SQL Server
- Có thể mở màn hình Submissions
- Có thể chạy submission C++ khi `g++` đã cài

## 9. Xử lý lỗi thường gặp

### Không kết nối được SQL Server

- Kiểm tra `db.url`
- Kiểm tra database `OJ_AI_TESTER` có tồn tại không
- Kiểm tra SQL Server đang chạy
- Kiểm tra `integratedSecurity=true` có phù hợp môi trường không

### Không tìm thấy `g++`

- Cài MinGW-w64
- Thêm `g++` vào PATH
- Mở lại terminal / IDE sau khi cập nhật PATH

### Lỗi API Gemini

- Kiểm tra `GEMINI_API_KEY` hoặc `ai.apiKey`
- Kiểm tra `ai.model`
- Đảm bảo máy có kết nối Internet

### Lỗi import ảnh / PDF

- Kiểm tra file nguồn có hợp lệ không
- Kiểm tra cấu hình OCR nếu môi trường của bạn cần Tesseract


