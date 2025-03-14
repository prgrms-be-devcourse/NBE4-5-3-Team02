"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { motion } from "framer-motion";

interface Window {
  daum: any;
}

interface IAddr {
  address: string;
  zonecode: string;
}

export default function CreatePostPage() {
  const router = useRouter();

  // 게시물 입력 상태
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [category, setCategory] = useState("ELECTRONICS");
  const [priceType, setPriceType] = useState("DAY");
  const [price, setPrice] = useState("");

  const [address, setAddress] = useState(""); // 주소 상태 추가
  const [zipCode, setZipCode] = useState(""); // 우편번호
  const [addressDetail, setAddressDetail] = useState(""); // 상세 주소
  const [latitude, setLatitude] = useState("");
  const [longitude, setLongitude] = useState("");
  const [availabilities, setAvailabilities] = useState([
    {
      startTime: "",
      endTime: "",
      date: "",
      isRecurring: false,
      recurrenceDays: 0,
    },
  ]);
  const [images, setImages] = useState<File[]>([]);
  const [loading, setLoading] = useState(false);

  // 카테고리 및 가격 타입 옵션
  const categoryOptions = ["ELECTRONICS", "TOOL"];
  const priceTypeOptions = ["HOUR", "DAY"];

  // 이용 가능 시간 추가 핸들러
  const addAvailability = () => {
    setAvailabilities([
      ...availabilities,
      {
        startTime: "",
        endTime: "",
        date: "",
        isRecurring: false,
        recurrenceDays: 0,
      },
    ]);
  };

  // 이용 가능 시간 삭제 핸들러
  const removeAvailability = (index: number) => {
    if (availabilities.length === 1) return; // 첫 번째 시간은 삭제 불가능
    const updatedAvailabilities = availabilities.filter((_, i) => i !== index);
    setAvailabilities(updatedAvailabilities);
  };

  // 이미지 업로드 핸들러 (최대 3개 제한)
  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      const selectedFiles = Array.from(e.target.files);

      if (images.length + selectedFiles.length > 3) {
        alert("이미지는 최대 3개까지 업로드 가능합니다.");
        return;
      }

      setImages([...images, ...selectedFiles]);
    }
  };

  // 이미지 삭제 핸들러
  const removeImage = (index: number) => {
    const updatedImages = images.filter((_, i) => i !== index);
    setImages(updatedImages);
  };

  // 이용 가능 시간 변경 핸들러
  const handleAvailabilityChange = (
    index: number,
    field: string,
    value: string | boolean | number
  ) => {
    setAvailabilities((prevAvailabilities) => {
      const updatedAvailabilities = [...prevAvailabilities];

      if (field === "isRecurring") {
        updatedAvailabilities[index] = {
          ...updatedAvailabilities[index],
          isRecurring: value as boolean,
          date: value ? "" : updatedAvailabilities[index].date, // 반복이면 date 초기화
          recurrenceDays: value
            ? updatedAvailabilities[index].recurrenceDays
            : 0,
        };
      } else if (field === "date") {
        // UI에서는 YYYY-MM-DD로 저장, 실제 저장 시 T00:00:00 추가
        updatedAvailabilities[index] = {
          ...updatedAvailabilities[index],
          date: value ? `${value}T00:00:00` : "", // 저장할 때만 T00:00:00 추가
        };
      } else {
        updatedAvailabilities[index] = {
          ...updatedAvailabilities[index],
          [field]: value,
        };
      }

      return updatedAvailabilities;
    });
  };

  const BASE_URL = process.env.NEXT_PUBLIC_BASE_URL;
    
  // 게시물 등록 요청 핸들러
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      const formData = new FormData();

      // ✅ JSON 데이터를 Blob으로 변환하여 추가
      const requestData = {
        title,
        content,
        category,
        priceType,
        price: Number(price),
        latitude: Number(latitude),
        longitude: Number(longitude),
        availabilities,
      };

      // Blob으로 변환 (Content-Type: application/json)
      const jsonBlob = new Blob([JSON.stringify(requestData)], {
        type: "application/json",
      });
      formData.append("request", jsonBlob);

      // 이미지 파일 추가
      images.forEach((image) => formData.append("images", image));

      // API 요청
      const response = await fetch(`${BASE_URL}/api/v1/posts`, {
        method: "POST",
        credentials: "include",
        body: formData,
      });

      if (!response.ok) throw new Error("게시물 등록 실패");

      router.push("/posts"); // 게시물 목록 페이지로 이동
    } catch (err) {
      console.error(err);
      alert("게시물 등록 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const onClickAddr = () => {
    new window.daum.Postcode({
      oncomplete: function (data: IAddr) {
        setAddress(data.address); // 주소 상태 업데이트
        setZipCode(data.zonecode); // 우편번호 상태 업데이트
        
        document.getElementById("addrDetail")?.focus(); // 상세 주소 입력 필드로 자동 포커스

        // 주소 → 위도/경도 변환 요청
        fetchCoordsFromAddress(data.address);
      },
    }).open();
  };

  const fetchCoordsFromAddress = async (address: string) => {
    try {
      const response = await fetch(
        `https://dapi.kakao.com/v2/local/search/address.json?query=${encodeURIComponent(
          address
        )}`,
        {
          method: "GET",
          headers: {
            Authorization: `KakaoAK ${process.env.NEXT_PUBLIC_KAKAO_REST_API_KEY}`,
          },
        }
      );
        const data = await response.json();
        
        console.log("카카오 API 응답:", data); // 전체 응답을 확인
        console.log("키 확인 : ",process.env.NEXT_PUBLIC_KAKAO_REST_API_KEY);

      if (data.documents.length > 0) {
        setLatitude(data.documents[0].y);
        setLongitude(data.documents[0].x);
      } else {
        alert("위도·경도를 찾을 수 없습니다. 다른 주소를 입력해주세요.");
      }
    } catch (error) {
      console.error("위도·경도 변환 실패:", error);
      alert("위도·경도 정보를 가져오는 데 실패했습니다.");
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 1.5 }}
      className="min-h-screen bg-gray-100 flex flex-col items-center py-10 px-4"
    >
      <h1 className="text-3xl font-bold text-gray-800 mb-6">📝 게시물 작성</h1>

      <form
        onSubmit={handleSubmit}
        className="bg-white p-6 rounded-2xl shadow-lg max-w-3xl w-full"
      >
        {/* 제목 입력 */}
        <div className="mb-4">
          <label className="block text-gray-700 font-semibold mb-2">제목</label>
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            className="w-full p-2 border rounded focus:ring-2 focus:ring-blue-500"
            required
          />
        </div>

        {/* 내용 입력 */}
        <div className="mb-4">
          <label className="block text-gray-700 font-semibold mb-2">내용</label>
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            className="w-full p-2 border rounded focus:ring-2 focus:ring-blue-500"
            rows={4}
            required
          />
        </div>

        {/* 카테고리 및 가격 타입 선택 */}
        <div className="flex gap-4 mb-4">
          <div className="flex-1">
            <label className="block text-gray-700 font-semibold mb-2">
              카테고리
            </label>
            <select
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              className="w-full p-2 border rounded"
            >
              {categoryOptions.map((cat) => (
                <option key={cat} value={cat}>
                  {cat}
                </option>
              ))}
            </select>
          </div>

          <div className="flex-1">
            <label className="block text-gray-700 font-semibold mb-2">
              가격 타입
            </label>
            <select
              value={priceType}
              onChange={(e) => setPriceType(e.target.value)}
              className="w-full p-2 border rounded"
            >
              {priceTypeOptions.map((type) => (
                <option key={type} value={type}>
                  {type}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* 가격 입력 */}
        <div className="mb-4">
          <label className="block text-gray-700 font-semibold mb-2">
            가격 (원)
          </label>
          <input
            type="number"
            value={price}
            onChange={(e) => setPrice(e.target.value)}
            className="w-full p-2 border rounded"
            required
          />
        </div>

        {/* 이용 가능 시간 추가 */}
        <div className="mb-4">
          <label className="block text-gray-700 font-semibold mb-2">
            이용 가능 시간
          </label>
          {availabilities.map((a, index) => (
            <div
              key={index}
              className="border p-4 rounded-lg mb-4 shadow-sm bg-gray-50"
            >
              {/* 단순 이용 가능 시간 (날짜, 시작시간, 종료시간) */}
              <div className="flex flex-col gap-2 mb-2">
                {!a.isRecurring && (
                  <input
                    type="date"
                    value={a.date ? a.date.split("T")[0] : ""} // T00:00:00 제거하고 UI에 표시
                    onChange={(e) =>
                      handleAvailabilityChange(index, "date", e.target.value)
                    }
                    className="p-2 border rounded w-full"
                    required
                  />
                )}
                <input
                  type="datetime-local"
                  value={a.startTime || ""}
                  onChange={(e) => {
                    console.log("Start Time Selected:", e.target.value);
                    handleAvailabilityChange(
                      index,
                      "startTime",
                      e.target.value
                    );
                  }}
                  className="p-2 border rounded w-full"
                  required
                />
                <input
                  type="datetime-local"
                  value={a.endTime || ""}
                  onChange={(e) => {
                    console.log("End Time Selected:", e.target.value);
                    handleAvailabilityChange(index, "endTime", e.target.value);
                  }}
                  className="p-2 border rounded w-full"
                  required
                />
              </div>

              {/* 반복 여부 선택 */}
              <div className="flex items-center gap-2 mb-2">
                <input
                  type="checkbox"
                  checked={a.isRecurring}
                  onChange={(e) =>
                    handleAvailabilityChange(
                      index,
                      "isRecurring",
                      e.target.checked
                    )
                  }
                  className="w-5 h-5"
                />
                <span className="text-gray-700">매 주 가능</span>
              </div>

              {/* 반복 예약인 경우 요일 선택 */}
              {a.isRecurring && (
                <div className="flex flex-wrap gap-2">
                  {[
                    { label: "월", value: 1 },
                    { label: "화", value: 2 },
                    { label: "수", value: 3 },
                    { label: "목", value: 4 },
                    { label: "금", value: 5 },
                    { label: "토", value: 6 },
                    { label: "일", value: 7 },
                  ].map((day) => (
                    <label key={day.value} className="flex items-center gap-1">
                      <input
                        type="radio"
                        name={`recurrence-${index}`}
                        value={day.value}
                        checked={a.recurrenceDays === day.value}
                        onChange={(e) =>
                          handleAvailabilityChange(
                            index,
                            "recurrenceDays",
                            Number(e.target.value)
                          )
                        }
                        className="w-5 h-5"
                      />
                      {day.label}
                    </label>
                  ))}
                </div>
              )}

              {/* 삭제 버튼 (첫 번째 시간 제외) */}
              {index > 0 && (
                <button
                  type="button"
                  onClick={() => removeAvailability(index)}
                  className="text-red-500 hover:text-red-700 mt-2"
                >
                  ❌ 삭제
                </button>
              )}
            </div>
          ))}
          <button
            type="button"
            onClick={addAvailability}
            className="text-blue-600 mt-2"
          >
            + 이용 가능 시간 추가
          </button>
        </div>

        {/* 이미지 업로드 버튼 및 리스트 */}
        <div className="mb-4">
          <label className="block text-gray-700 font-semibold mb-2">
            이미지 업로드 (최대 3개)
          </label>
          <input
            type="file"
            multiple
            accept="image/*"
            onChange={handleImageChange}
            className="hidden"
            id="fileInput"
          />

          {/* 커스텀 업로드 버튼 */}
          <label
            htmlFor="fileInput"
            className="block w-full bg-blue-500 text-white text-center py-2 rounded-lg cursor-pointer hover:bg-blue-600"
          >
            이미지 선택
          </label>

          {/* 업로드된 이미지 파일명 표시 */}
          <div className="mt-2">
            {images.map((image, index) => (
              <div
                key={index}
                className="flex justify-between items-center bg-gray-200 px-3 py-1 rounded-md mb-1"
              >
                <span className="text-gray-700">{image.name}</span>
                <button
                  type="button"
                  onClick={() => removeImage(index)}
                  className="text-red-500 hover:text-red-700"
                >
                  ❌
                </button>
              </div>
            ))}
          </div>
        </div>

        {/* 카카오 주소 검색 */}

        <div className="mb-4">
          <label className="block text-gray-700 font-semibold mb-2">
            주소 검색
          </label>

          <div className="flex gap-2">
            <input
              id="addr"
              type="text"
              value={address}
              readOnly
              placeholder="주소를 검색하세요"
              className="flex-1 p-2 border rounded bg-gray-100 cursor-pointer focus:ring-2 focus:ring-blue-500"
              onClick={onClickAddr}
            />
            <button
              type="button"
              onClick={onClickAddr}
              className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition"
            >
              검색
            </button>
          </div>
          {/* 우편번호 & 상세 주소 입력 */}
          <div className="mt-2 flex gap-2">
            <input
              id="zipNo"
              type="text"
              value={zipCode}
              readOnly
              placeholder="우편번호"
              className="p-2 border rounded bg-gray-100 w-1/3"
            />
            <input
              id="addrDetail"
              type="text"
              value={addressDetail}
              onChange={(e) => setAddressDetail(e.target.value)}
              placeholder="상세 주소 입력"
              className="p-2 border rounded flex-1"
            />
          </div>
              </div>
              
              {/* 위도/경도 입력 */}
        <div className="flex gap-4 mb-4">
          <input
            type="text"
            value={latitude}
            onChange={(e) => setLatitude(e.target.value)}
            placeholder="위도 (Latitude)"
            className="flex-1 p-2 border rounded"
            required
          />
          <input
            type="text"
            value={longitude}
            onChange={(e) => setLongitude(e.target.value)}
            placeholder="경도 (Longitude)"
            className="flex-1 p-2 border rounded"
            required
          />
        </div>
        <body>
          <script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
        </body>
        <button
          type="submit"
          className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition"
          disabled={loading}
        >
          {loading ? "등록 중..." : "게시물 등록"}
        </button>
      </form>
    </motion.div>
  );
}
