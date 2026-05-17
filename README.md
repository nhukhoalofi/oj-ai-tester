# OJ AI Tester

OJ AI Tester là ứng dụng JavaFX hỗ trợ quản lý đề bài lập trình, nhập đề từ file, phân tích đề bằng AI, sinh testcase, lưu submission mẫu và chạy code C++ trên bộ testcase để kiểm tra kết quả.

## Chức năng chính

- Quản lý danh sách đề bài trong SQL Server
- Import đề bài từ file text / image / PDF
- Load text từ file để trích xuất nội dung đề
- Phân tích đề bằng AI (Gemini)
- Sinh testcase cho problem
- Lưu submission mẫu AC / WA / TLE
- Compile và chạy source code C++
- Chấm kết quả theo AC / WA / TLE / RE / CE
- Lưu kết quả chạy vào database
- Xem chi tiết problem, testcase, submission và kết quả chạy

## Công nghệ sử dụng

- Java 17
- JavaFX
- Maven
- JDBC
- Microsoft SQL Server JDBC Driver
- SQL Server
- Gemini API

## Tài liệu

- [Hướng dẫn cài đặt](docs/INSTALLATION.md)
- [Hướng dẫn sử dụng](docs/USER_GUIDE.md)

## Ghi chú

- Project hiện đang kết nối tới SQL Server local qua JDBC.
- Chức năng chạy C++ yêu cầu máy cài `g++` hoặc MinGW-w64.
- Nếu dùng import từ ảnh / PDF, cần cấu hình đúng phần trích xuất văn bản trong ứng dụng.

