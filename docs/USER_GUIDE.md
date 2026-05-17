# Hướng dẫn sử dụng

Tài liệu này mô tả cách sử dụng các màn hình chính của **OJ AI Tester**.

## 1. Tổng quan giao diện

Sau khi mở ứng dụng, bạn sẽ thấy thanh điều hướng bên trái gồm các mục:

- Add Problem
- Problem List
- AI Analysis
- Testcases
- Submissions
- Evaluation

Các màn hình này phục vụ cho luồng làm việc chính:

1. Thêm đề bài
2. Xem danh sách đề
3. Phân tích đề bằng AI
4. Sinh testcase
5. Lưu submission mẫu và chạy code C++
6. Đánh giá testcase

---

## 2. Màn hình Add Problem

Màn hình này dùng để thêm đề bài mới vào database.

### 2.1. Nhập đề theo kiểu TEXT

1. Chọn `TEXT` ở phần `Source Type`.
2. Nhập `Title`.
3. Nhập toàn bộ nội dung đề bài vào ô `Raw Text`.
4. Bấm **Save Problem**.

### 2.2. Nhập đề từ IMAGE / PDF

1. Chọn hoặc tải file ảnh / PDF của đề bài.
2. Bấm **Load Text From File** để trích xuất nội dung.
3. Kiểm tra lại phần text đã được load, có thể chỉnh sửa thủ công nếu cần.
4. Nhập `Title`.
5. Bấm **Save Problem**.

### 2.3. Trạng thái lưu

Khi lưu thành công, problem sẽ được ghi vào bảng `problems` với:

- title
- raw_text
- source_type
- source_path
- status

---

## 3. Màn hình Problem List

Màn hình này dùng để xem danh sách đề đã lưu trong database.

### 3.1. Xem danh sách đề

- Bảng bên trái hiển thị toàn bộ problem.
- Bạn có thể chọn từng dòng để xem chi tiết.

### 3.2. Xem chi tiết đề

Khi chọn một problem, phần chi tiết sẽ hiển thị:

- tiêu đề
- loại nguồn (`TEXT`, `IMAGE`, `PDF`)
- trạng thái
- ngày tạo
- tên file nguồn
- nội dung raw text

Nếu đề chưa có raw text, màn hình sẽ hiển thị thông báo tương ứng thay vì để trống.

### 3.3. Mở file nguồn gốc

Nếu problem có `source_path`, bạn có thể mở file gốc bằng nút **Open Source File**.

### 3.4. Xóa đề

Nút **Delete** sẽ xóa problem và các dữ liệu liên quan nếu project hỗ trợ.

---

## 4. Màn hình AI Analysis

Màn hình này dùng để phân tích đề bài bằng AI và lưu nội dung đã phân tích vào database.

### Cách dùng

1. Chọn một problem trong danh sách.
2. Xem lại raw text của đề.
3. Bấm **Analyze**.
4. Chờ AI xử lý và lưu kết quả.

### Kết quả phân tích

Sau khi phân tích thành công, hệ thống có thể lưu các trường như:

- title đã chuẩn hóa
- statement
- input format
- output format
- constraints
- tags
- summary

---

## 5. Màn hình Testcases

Màn hình này dùng để sinh và quản lý testcase cho từng problem.

### Cách dùng

1. Chọn một problem.
2. Bấm **Generate Testcases**.
3. Chờ AI sinh testcase.
4. Xem danh sách testcase trong bảng.

### Thao tác có thể làm

- Refresh testcase của problem hiện tại
- Xóa một testcase
- Xóa toàn bộ testcase của problem

### Dữ liệu testcase

Mỗi testcase thường gồm:

- category
- input_data
- expected_output
- purpose
- strength_score

---

## 6. Màn hình Submissions

Đây là màn hình dùng để lưu submission mẫu và chạy code C++ trên testcase của problem.

### 6.1. Chọn problem

1. Chọn một problem ở combobox `Problem`.
2. Danh sách submission bên dưới sẽ được nạp theo problem đó.

### 6.2. Nhập submission mẫu

Bạn cần nhập:

- `Name`
- `Type` (`AC`, `WA`, `TLE`)
- `Language` (thường là `C++`)
- `Source Code`

### 6.3. Lưu submission

1. Kiểm tra lại problem đã chọn.
2. Nhập tên và source code.
3. Bấm **Save**.

Sau khi lưu thành công, submission sẽ được ghi vào bảng `submissions`.

### 6.4. Chạy submission

1. Chọn một submission đã lưu hoặc nhập code mới.
2. Bấm **Run**.
3. Hệ thống sẽ compile code C++.
4. Chạy trên toàn bộ testcase của problem đang chọn.
5. So sánh output thực tế với expected output.

### 6.5. Trạng thái kết quả

Kết quả chạy có thể là:

- `AC` — đúng hoàn toàn
- `WA` — sai đáp án
- `TLE` — quá thời gian
- `RE` — lỗi runtime
- `CE` — lỗi biên dịch

### 6.6. Xem kết quả chi tiết

- Bảng `Run Results by testcase` hiển thị kết quả từng testcase.
- Khi chọn một dòng kết quả, bạn có thể xem:
  - input
  - expected output
  - actual output
  - error message

---

## 7. Màn hình Evaluation

Màn hình này dùng để đánh giá độ mạnh của testcase dựa trên các submission mẫu.

### Cách dùng

1. Chọn một problem.
2. Đảm bảo problem đó đã có:
   - ít nhất một submission `AC`
   - ít nhất một submission `WA` hoặc `TLE`
   - testcase đã tồn tại
3. Bấm **Run Evaluation**.

### Kết quả đánh giá

Hệ thống sẽ thống kê:

- tổng số testcase
- số testcase AC pass
- số submission WA/TLE bị phát hiện
- điểm strength score

---

## 8. Mẹo sử dụng nhanh

- Luôn lưu problem trước khi sang AI Analysis / Testcases / Submissions.
- Với đề IMAGE/PDF, hãy bấm **Load Text From File** trước khi lưu.
- Với Submissions, hãy chọn đúng problem trước khi lưu hoặc chạy.
- Nếu run code C++ lỗi, hãy kiểm tra `g++` đã cài và có trong PATH chưa.

## 9. Giới hạn hiện tại

- Chức năng chạy C++ phụ thuộc vào `g++` bên ngoài.
- Project dùng database thật, không có dữ liệu mock.
- Một số thao tác AI phụ thuộc vào kết nối Internet và API Gemini.


