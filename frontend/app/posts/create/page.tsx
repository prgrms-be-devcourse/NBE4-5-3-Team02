"use client";

import {useState} from "react";
import {useRouter} from "next/navigation";
import {motion} from "framer-motion";
import {CalendarDaysIcon, CheckIcon, MoonIcon, PlusCircleIcon, SunIcon, TagIcon, TrashIcon} from "lucide-react";
import {
    CloudArrowUpIcon,
    CurrencyYenIcon,
    DocumentDuplicateIcon,
    PhotoIcon,
    XMarkIcon
} from "@heroicons/react/16/solid";
import {ArrowPathIcon, ClockIcon, MagnifyingGlassIcon, SparklesIcon} from "@heroicons/react/24/outline";
import {fetchWithAuth} from "@/app/lib/util/fetchWithAuth";
import {MapPinIcon} from "@heroicons/react/24/solid";

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
            const response = await fetchWithAuth("http://localhost:8080/api/v1/posts", {
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
                document.getElementById("addrDetail")?.focus(); // ✅ 상세 주소 입력 필드로 자동 포커스

        // 주소 → 위도/경도 변환 요청
        fetchCoordsFromAddress(data.address);
      },
      // @ts-expect-error: 'open' 메서드에서 타입 오류 발생 가능성 있음
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
            console.log("키 확인 : ", process.env.NEXT_PUBLIC_KAKAO_REST_API_KEY);

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
            initial={{opacity: 0}}
            animate={{opacity: 1}}
            transition={{duration: 1.5}}
            className="min-h-screen flex flex-col items-center py-10 px-4"
        >

            <motion.h1
                className="text-3xl text-gray-600 font-bold mb-4 text-center"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ duration: 0.5 }}
            >
                🛍️ 물품 등록
            </motion.h1>

            <motion.p
                className="text-lg text-gray-600 mb-8 text-center"
                initial={{ y: -20, opacity: 0 }}
                animate={{ y: 0, opacity: 1 }}
                transition={{ duration: 0.5 }}
            >
                물품 정보를 상세히 입력해주세요
            </motion.p>

            {/* 폼 컨테이너 */}
            <div className="w-full max-w-3xl space-y-6">
                {/* 카드형 입력 섹션 */}
                <div className="p-6 rounded-xl border border-gray-200 bg-white/50 backdrop-blur-sm shadow-xl">
                    <form
                        onSubmit={handleSubmit}
                        className="bg-white p-6 rounded-2xl shadow-lg max-w-3xl w-full"
                    >
                        {/* 제목 입력 */}
                        <div className="mb-6 relative">
                            <input
                                type="text"
                                value={title}
                                onChange={(e) => setTitle(e.target.value)}
                                className="w-full px-4 py-3 border-2 border-gray-200 rounded-lg
      focus:border-gradient-to-r from-green-400 to-green-600
      focus:ring-0 focus:shadow-lg transition-all duration-300
      placeholder-transparent peer text-gray-700"
                                placeholder=" "
                                required
                            />
                            <label className="absolute left-4 top-3 text-gray-400
    peer-placeholder-shown:text-gray-400
    peer-focus:-translate-y-5 peer-focus:text-sm
    peer-focus:text-green-500 transition-all duration-300
    pointer-events-none bg-white px-1">
                                제목
                            </label>
                        </div>

                        {/* 내용 입력 */}
                        <div className="mb-6 relative">
  <textarea
      value={content}
      onChange={(e) => setContent(e.target.value)}
      className="w-full px-4 py-3 border-2 border-gray-200 rounded-lg
      focus:border-gradient-to-r from-green-400 to-green-500
      focus:ring-0 focus:shadow-lg transition-all duration-300
      placeholder-transparent peer resize-none text-gray-700"
      rows={4}
      placeholder=" "
      required
  />
                            <label className="absolute left-4 top-3 text-gray-700
    peer-placeholder-shown:text-gray-400
    peer-focus:-translate-y-5 peer-focus:text-sm
    peer-focus:text-green-500 transition-all duration-300
    pointer-events-none bg-white px-1">
                                내용
                            </label>
                            <div className="absolute bottom-3 right-4 text-sm text-gray-700">
                                {content.length}/500자
                            </div>
                        </div>

                        <div className="mb-8 space-y-4 animate-fade-in-up">
                            <h3 className="text-lg font-bold text-gray-800 flex items-center">
                                <SparklesIcon className="w-5 h-5 mr-2 text-emerald-600 animate-pulse" />
                                <span className="bg-gradient-to-r from-emerald-600 to-green-500 bg-clip-text text-transparent">
      상품 카테고리
    </span>
                            </h3>

                            <div className="flex flex-wrap gap-2">
                                {categoryOptions.map((cat) => (
                                    <button
                                        key={cat}
                                        onClick={() => setCategory(cat)}
                                        className={`px-4 py-2 rounded-full transition-all duration-300 
          ${
                                            category === cat
                                                ? 'bg-emerald-600 text-white shadow-lg transform scale-105'
                                                : 'bg-emerald-50 hover:bg-emerald-100 text-gray-800'
                                        }
          hover:scale-105 hover:shadow-md focus:outline-none focus:ring-2 
          focus:ring-emerald-500 focus:ring-offset-2`}
                                    >
        <span className="flex items-center space-x-2">
          {category === cat && (
              <CheckIcon className="w-4 h-4 animate-bounce" />
          )}
            <span className="whitespace-nowrap">{cat}</span>
        </span>
                                    </button>
                                ))}
                            </div>
                        </div>

                        {/* 가격 타입 & 범위 선택 */}
                        <div className="grid md:grid-cols-2 gap-6 mb-8">
                            <div className="space-y-4">
                                    <h3 className="text-lg font-bold text-green-600 flex items-center">
                                    <CurrencyYenIcon className="w-5 h-5 mr-2" />
                                    가격 유형
                                </h3>

                                <div className="grid grid-cols-2 gap-3">
                                    {priceTypeOptions.map((type) => (
                                        <label
                                            key={type}
                                            className={`flex items-center p-3 rounded-lg border cursor-pointer transition-colors text-gray-700 ${
                                                priceType === type
                                                    ? 'border-green-500 bg-green-50'
                                                    : 'border-gray-200 hover:border-green-300'
                                            }`}
                                        >
                                            <input
                                                type="radio"
                                                value={type}
                                                checked={priceType === type}
                                                onChange={(e) => setPriceType(e.target.value)}
                                                className="sr-only"
                                            />
                                            <span className="ml-2">{type}</span>
                                        </label>
                                    ))}
                                </div>
                            </div>

                            <div className="space-y-4">
                                <h3 className="text-lg font-bold text-green-600 flex items-center">
                                    <TagIcon className="w-5 h-5 mr-2" />
                                    가격 설정
                                </h3>

                                <div className="relative">
                                    <input
                                        type="range"
                                        min="0"
                                        max="100000"
                                        step="1000"
                                        value={price}
                                        onChange={(e) => setPrice(e.target.value)}
                                        className="w-full range-lg accent-green-500"
                                    />
                                    <div className="flex justify-between text-sm text-gray-500 mt-2">
                                        <span>0원</span>
                                        <div className="flex items-center">
                                            <span className="mr-2">선택가격:</span>
                                            <span className="font-bold text-green-600">
            {Number(price).toLocaleString()}원
          </span>
                                        </div>
                                        <span>100,000원</span>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="space-y-6">
                            <label className="block text-lg font-bold text-gray-800 mb-4">
                                <ClockIcon className="w-6 h-6 inline-block mr-2 text-emerald-600" />
                                <span className="bg-gradient-to-r from-emerald-600 to-green-500 bg-clip-text text-transparent">
      이용 가능 시간 설정
    </span>
                            </label>

                            {availabilities.map((a, index) => (
                                <div
                                    key={index}
                                    className="border-2 border-emerald-100 rounded-xl p-6 bg-white shadow-lg hover:shadow-xl transition-shadow duration-300"
                                >
                                    {/* 시간 선택 섹션 */}
                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                                        {!a.isRecurring && (
                                            <div className="relative">
                                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                                    <CalendarDaysIcon className="w-4 h-4 inline-block mr-1 text-emerald-600" />
                                                    날짜 선택
                                                </label>
                                                <input
                                                    type="date"
                                                    value={a.date?.split('T')[0] || ''}
                                                    onChange={(e) => handleAvailabilityChange(index, 'date', e.target.value)}
                                                    className="w-full px-4 py-3 border-2 border-emerald-100 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all text-gray-700"
                                                />
                                            </div>
                                        )}

                                        <div className="space-y-4">
                                            <div className="relative">
                                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                                    <SunIcon className="w-4 h-4 inline-block mr-1 text-emerald-600" />
                                                    시작 시간
                                                </label>
                                                <input
                                                    type="datetime-local"
                                                    value={a.startTime}
                                                    onChange={(e) => handleAvailabilityChange(index, 'startTime', e.target.value)}
                                                    className="w-full px-4 py-3 border-2 border-emerald-100 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all text-gray-700"
                                                />
                                            </div>

                                            <div className="relative">
                                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                                    <MoonIcon className="w-4 h-4 inline-block mr-1 text-emerald-600" />
                                                    종료 시간
                                                </label>
                                                <input
                                                    type="datetime-local"
                                                    value={a.endTime}
                                                    onChange={(e) => handleAvailabilityChange(index, 'endTime', e.target.value)}
                                                    className="w-full px-4 py-3 border-2 border-emerald-100 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all text-gray-700"
                                                />
                                            </div>
                                        </div>
                                    </div>

                                    {/* 반복 설정 섹션 */}
                                    <div className="flex items-center space-x-4 mb-6">
                                        <button
                                            type="button"
                                            onClick={(e) => handleAvailabilityChange(index, 'isRecurring', !a.isRecurring)}
                                            className={`flex items-center space-x-2 px-4 py-2 rounded-full transition-colors ${
                                                a.isRecurring
                                                    ? 'bg-emerald-600 text-white'
                                                    : 'bg-emerald-50 hover:bg-emerald-100 text-gray-700'
                                            }`}
                                        >
                                            <ArrowPathIcon className="w-5 h-5" />
                                            <span>매주 반복</span>
                                        </button>

                                        {a.isRecurring && (
                                            <div className="flex flex-wrap gap-2 ml-4">
                                                {[
                                                    { label: '월', value: 1 },
                                                    { label: '화', value: 2 },
                                                    { label: '수', value: 3 },
                                                    { label: '목', value: 4 },
                                                    { label: '금', value: 5 },
                                                    { label: '토', value: 6 },
                                                    { label: '일', value: 7 }
                                                ].map((day) => (
                                                    <button
                                                        key={day.value}
                                                        type="button"
                                                        onClick={() => handleAvailabilityChange(index, 'recurrenceDays', day.value)}
                                                        className={`w-10 h-10 rounded-full flex items-center justify-center transition-all ${
                                                            a.recurrenceDays === day.value
                                                                ? 'bg-emerald-600 text-white shadow-lg'
                                                                : 'bg-emerald-50 hover:bg-emerald-100 text-gray-700'
                                                        }`}
                                                    >
                                                        {day.label}
                                                    </button>
                                                ))}
                                            </div>
                                        )}
                                    </div>

                                    {/* 삭제 버튼 */}
                                    {index > 0 && (
                                        <div className="w-full mt-6">
                                            <div className="flex justify-end border-t border-emerald-100 pt-4">
                                                <button
                                                    type="button"
                                                    onClick={() => {
                                                        if (confirm('정말 삭제하시겠습니까?')) {
                                                            removeAvailability(index)
                                                        }
                                                    }}
                                                    className="flex items-center gap-x-1.5 px-3 py-1.5 text-red-500 hover:text-red-700 hover:bg-red-50 rounded-lg transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 "
                                                >
                                                    <TrashIcon className="w-5 h-5" />
                                                    <span className="font-medium">삭제</span>
                                                </button>
                                            </div>
                                        </div>
                                    )}
                                </div>
                            ))}

                            <div className="flex justify-end">
                                <button
                                    type="button"
                                    onClick={addAvailability}
                                    className="flex items-center bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2 rounded-lg transition-all duration-300 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:ring-offset-2 mb-6"
                                >
                                    <PlusCircleIcon className="w-5 h-5 mr-2" />
                                    <span className="font-bold">이용 가능 시간 추가</span>
                                </button>
                            </div>
                        </div>

                        <div className="mb-8 space-y-4 animate-fade-in-up">
                            <label className="block text-lg font-bold text-gray-800">
                                <PhotoIcon className="w-6 h-6 inline-block mr-2 text-emerald-600" />
                                <span className="bg-gradient-to-r from-emerald-600 to-green-500 bg-clip-text text-transparent">
      이미지 업로드
    </span>
                                <span className="block text-sm text-emerald-600 mt-1">(최대 3개)</span>
                            </label>

                            <input
                                type="file"
                                multiple
                                accept="image/*"
                                onChange={handleImageChange}
                                className="hidden"
                                id="imageUpload"
                            />

                            {/* 커스텀 업로드 영역 */}
                            <label
                                htmlFor="imageUpload"
                                className="group relative flex flex-col items-center justify-center h-40 border-4 border-dashed border-emerald-200 rounded-2xl bg-emerald-50 hover:bg-emerald-100 transition-colors cursor-pointer"
                            >
                                <div className="space-y-2 text-center">
                                    <CloudArrowUpIcon className="w-8 h-8 mx-auto text-emerald-600 group-hover:animate-bounce" />
                                    <p className="font-medium text-emerald-700">
                                        클릭 또는 드래그하여 업로드
                                    </p>
                                    <p className="text-sm text-emerald-500">PNG, JPG, JPEG 파일 지원</p>
                                </div>
                            </label>

                            {/* 이미지 미리보기 그리드 */}
                            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                {images.map((image, index) => (
                                    <div
                                        key={index}
                                        className="relative group border-2 border-emerald-100 rounded-xl overflow-hidden shadow-lg hover:shadow-xl transition-all"
                                    >
                                        <img
                                            src={URL.createObjectURL(image)}
                                            alt="미리보기"
                                            className="w-full h-32 object-cover"
                                        />
                                        <div className="p-3 bg-white">
                                            <p className="text-sm text-gray-700 truncate">{image.name}</p>
                                            <span className="text-xs text-emerald-600">
            {(image.size / 1024).toFixed(1)}KB
          </span>
                                        </div>
                                        <button
                                            type="button"
                                            onClick={() => removeImage(index)}
                                            className="absolute top-2 right-2 p-1 bg-white/90 rounded-full hover:bg-red-100 transition-colors"
                                        >
                                            <XMarkIcon className="w-5 h-5 text-red-500 hover:text-red-700" />
                                        </button>
                                    </div>
                                ))}
                            </div>
                        </div>

                        {/* 카카오 주소 검색 */}
                        <div className="mb-8 space-y-4 animate-fade-in-up">
                            <label className="block text-lg font-bold text-gray-800">
                                <MapPinIcon className="w-6 h-6 inline-block mr-2 text-emerald-600" />
                                <span className="bg-gradient-to-r from-emerald-600 to-green-500 bg-clip-text text-transparent">
      주소 검색
    </span>
                            </label>

                            <div className="grid grid-cols-[1fr_auto] gap-2">
                                <input
                                    id="addr"
                                    type="text"
                                    value={address}
                                    readOnly
                                    placeholder="주소를 검색하세요"
                                    className="w-full px-4 py-3 border-2 border-emerald-100 rounded-xl bg-white focus:ring-2 focus:ring-emerald-500 focus:border-transparent cursor-pointer transition-all text-gray-700 placeholder:text-gray-700"
                                    onClick={onClickAddr}
                                />
                                <button
                                    type="button"
                                    onClick={onClickAddr}
                                    className="px-6 py-3 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl transition-all duration-300 hover:shadow-lg flex items-center justify-center gap-x-2"
                                >
                                    <MagnifyingGlassIcon className="w-5 h-5" />
                                    <span>검색</span>
                                </button>
                            </div>

                            {/* 우편번호 & 상세 주소 입력 */}
                            <div className="grid grid-cols-1 md:grid-cols-[1fr_2fr] gap-2">
                                <div className="relative">
                                    <input
                                        id="zipNo"
                                        type="text"
                                        value={zipCode}
                                        readOnly
                                        placeholder="우편번호"
                                        className="w-full px-4 py-3 border-2 text-gray-700 border-emerald-100 rounded-xl bg-gray-50 placeholder:text-gray-700"
                                    />
                                    <DocumentDuplicateIcon className="w-5 h-5 absolute right-4 top-3.5 text-emerald-600" />
                                </div>
                                <input
                                    id="addrDetail"
                                    type="text"
                                    value={addressDetail}
                                    onChange={(e) => setAddressDetail(e.target.value)}
                                    placeholder="상세 주소를 입력해주세요"
                                    className="w-full px-4 py-3 border-2 border-emerald-100 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all placeholder:text-gray-700"
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
                            className="w-full bg-emerald-600 hover:bg-emerald-700 text-white font-bold py-2.5 rounded-xl transition-all duration-300 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
                            disabled={loading}
                        >
                            {loading ? (
                                <div className="flex items-center justify-center gap-2">
                                    <ArrowPathIcon className="w-5 h-5 animate-spin" />
                                    <span>등록 중...</span>
                                </div>
                            ) : (
                                "게시물 등록"
                            )}
                        </button>
                    </form>
                </div>
            </div>
        </motion.div>
    );
}
