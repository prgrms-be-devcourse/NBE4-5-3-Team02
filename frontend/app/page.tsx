"use client";
import {useState} from 'react';
import {motion, AnimatePresence} from "framer-motion";
import Link from 'next/link';
import {
    ArrowPathIcon,
    HeartIcon,
    GlobeAltIcon,
    SparklesIcon,
    MapPinIcon,
    MagnifyingGlassIcon,
    UserPlusIcon
} from "@heroicons/react/24/outline";

export default function MainPage() {
    const [showBenefits, setShowBenefits] = useState(false);
    const [searchQuery, setSearchQuery] = useState('');
    const [selectedDistrict, setSelectedDistrict] = useState('');
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);

    const districts = ['강남구', '서초구', '송파구', '강동구', '마포구']; // 예시 지역구 목록
    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        // 검색 로직 구현
        console.log('검색어:', searchQuery, '선택된 지역구:', selectedDistrict);
    };

    return (
        <motion.div
            initial={{opacity: 0}}
            animate={{opacity: 1}}
            transition={{duration: 2}}
            style={{
                background: 'linear-gradient(135deg, #d4f1c4, #a7e3e0)',
                minHeight: '100vh',
                width: '100%',
                overflowY: 'auto'
            }}
        >
            {/* 변경된 검색창 섹션 */}
            <section className="flex justify-center p-6"> {/* 중앙 정렬 추가 */}
                <form
                    onSubmit={handleSearch}
                    className="flex items-center bg-white rounded-full px-6 py-4 shadow-xl w-full max-w-3xl border-2 border-green-100" // 크기 및 색상 변경
                >
                    {/* 지역 선택 드롭다운 */}
                    <div className="relative">
                        <button
                            type="button"
                            onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                            className="flex items-center text-sm font-medium text-green-800 focus:outline-none" // 색상 변경
                        >
                            <MapPinIcon className="w-6 h-6 mr-2 text-green-600"/> {/* 아이콘 색상 변경 */}
                            {selectedDistrict}
                        </button>
                        {isDropdownOpen && (
                            <ul className="absolute left-0 mt-2 w-32 bg-green-50 rounded-lg shadow-lg z-10 border border-green-100"> {/* 색상 변경 */}
                                {districts.map((district) => (
                                    <li
                                        key={district}
                                        onClick={() => {
                                            setSelectedDistrict(district);
                                            setIsDropdownOpen(false);
                                        }}
                                        className="px-4 py-2 hover:bg-green-100 cursor-pointer text-green-800" // 색상 변경
                                    >
                                        {district}
                                    </li>
                                ))}
                            </ul>
                        )}
                    </div>

                    {/* 구분선 */}
                    <span className="mx-4 text-green-300">|</span> {/* 색상 변경 */}

                    {/* 검색어 입력 */}
                    <input
                        type="text"
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        placeholder="검색어를 입력해주세요"
                        className="flex-grow bg-transparent text-lg focus:outline-none placeholder-green-300" // 크기 및 색상 변경
                    />

                    {/* 검색 버튼 */}
                    <button
                        type="submit"
                        className="ml-4 p-3 bg-green-600 rounded-full hover:bg-green-700 transition-colors" // 색상 변경
                    >
                        <MagnifyingGlassIcon className="w-7 h-7 text-white"/> {/* 아이콘 크기 변경 */}
                    </button>
                </form>
            </section>

            {/* 인기 검색어 영역*/}
            <div className="mt-4 text-sm text-gray-600 text-center">
                오늘의 인기 검색어: 플레이 스테이션, 빔 프로젝터, 망치, 전동 드릴, 닌텐도 스위치
            </div>

            <motion.section
                initial={{opacity: 0}}
                animate={{opacity: 1}}
                className="container mx-auto px-4 py-20 text-center"
            >
                <motion.h1
                    initial={{opacity: 0, y: 20}}
                    animate={{opacity: 1, y: 0}}
                    transition={{duration: 1}}
                    className="text-4xl md:text-6xl font-bold mb-6 text-green-800 flex items-center justify-center">
                    <SparklesIcon className="h-10 w-10 text-yellow-400"/>
                    소유 경제의 종말
                </motion.h1>

                <motion.div
                    initial={{y: 20, opacity: 0}}
                    animate={{y: 0, opacity: 1}}
                    className="space-y-8"
                >
                    <p className="text-xl md:text-2xl text-gray-600">
                        소유에서 공유로의 전환은 단순한 선택이 아닌<br/>
                        <span className="text-green-600 font-semibold">미래 세대를 위한 필수 의무입니다</span>
                    </p>
                    <motion.button
                        whileHover={{scale: 1.05}}
                        whileTap={{scale: 0.95}}
                        onClick={() => setShowBenefits(!showBenefits)}
                        className="bg-green-600 text-white px-8 py-4 rounded-full text-lg font-semibold shadow-lg hover:bg-green-700 hover:shadow-xl transition-all"
                    >
                        {showBenefits ? '기본 화면 보기' : '왜 공유인가요? →'}
                    </motion.button>
                </motion.div>
            </motion.section>

            <AnimatePresence>
                {showBenefits && (
                    <motion.section
                        initial={{opacity: 0, y: 50}}
                        animate={{opacity: 1, y: 0}}
                        exit={{opacity: 0, y: -50}}
                        className="container mx-auto px-4 py-16"
                    >
                        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8 mb-12">
                            {[
                                {
                                    icon: ArrowPathIcon,
                                    title: "경제적 가치 창출",
                                    value: "₩720,000+",
                                    desc: "연간 최대 절감액으로 가계 경제에 도움을 줍니다.",
                                    color: "from-green-400 to-green-300",
                                    iconBg: "bg-green-600"
                                },
                                {
                                    icon: HeartIcon,
                                    title: "사회적 관계 증진",
                                    value: "40%↑",
                                    desc: "이웃 간 신뢰도 개선, 더 나은 커뮤니티를 만듭니다.",
                                    color: "from-teal-400 to-teal-300",
                                    iconBg: "bg-teal-600"
                                },
                                {
                                    icon: GlobeAltIcon,
                                    title: "환경적 영향",
                                    value: "18kg↓",
                                    desc: "연간 CO₂ 배출량 감소로 환경 보호에 기여합니다.",
                                    color: "from-emerald-400 to-emerald-300",
                                    iconBg: "bg-emerald-600"
                                }
                            ].map((benefit, idx) => (
                                <motion.div
                                    key={idx}
                                    className={`p-8 rounded-3xl bg-gradient-to-br ${benefit.color} transition-all duration-300 hover:shadow-2xl`}
                                    initial={{ opacity: 0, y: 20 }}
                                    animate={{ opacity: 1, y: 0 }}
                                    transition={{ duration: 0.5, delay: idx * 0.2 }}
                                    whileHover={{
                                        scale: 1.03,
                                        boxShadow: "0 25px 50px -12px rgba(0, 0, 0, 0.25)"
                                    }}
                                >
                                    <div className="flex flex-col items-center text-center h-full justify-between">
                                        <div>
                                            <div className="mb-6 mx-auto">
                                                <benefit.icon className={`h-20 w-20 text-white p-5 rounded-full ${benefit.iconBg} shadow-lg`}/>
                                            </div>

                                            <h3 className="text-2xl font-bold mb-4 text-gray-800">
                                                {benefit.title}
                                            </h3>

                                            <div className="text-5xl font-bold mb-4 text-white">
                                                {benefit.value}
                                            </div>

                                            <p className="text-gray-700 text-base mt-2 leading-relaxed">
                                                {benefit.desc}
                                            </p>
                                        </div>
                                    </div>
                                </motion.div>
                            ))}
                        </div>

                        <motion.div
                            initial={{opacity: 0, y: 20}}
                            animate={{opacity: 1, y: 0}}
                            transition={{duration: 0.5}}
                            className="text-center bg-white p-8 md:p-12 rounded-3xl shadow-2xl max-w-2xl mx-auto"
                        >
                            <h2 className="text-3xl md:text-4xl font-bold mb-6 text-gray-800 flex items-center justify-center">
                                <UserPlusIcon className="h-10 w-10 md:h-12 md:w-12 mr-3 text-green-600"/>
                                지금 바로 시작하세요!
                            </h2>
                            <p className="text-lg md:text-xl mb-8 text-gray-700 leading-relaxed">
                                더 이상 물건을 쌓아두지 마세요.<br/>
                                <span className="font-semibold text-green-600">진정한 공유 경제의 일원</span>이 되어보세요!
                            </p>
                            <div className="space-y-4 md:space-y-0 md:space-x-4 flex flex-col md:flex-row justify-center">
                                <Link href="/signup">
                                    <motion.a
                                        whileHover={{scale: 1.05}}
                                        whileTap={{scale: 0.95}}
                                        animate={{y: [0, -5, 0]}}
                                        transition={{repeat: Infinity, duration: 2}}
                                        className="bg-green-600 text-white px-8 py-4 rounded-full text-lg font-bold shadow-lg hover:bg-green-700 transition-colors duration-300 inline-block"
                                    >
                                        지금 가입하고 혜택 받기
                                    </motion.a>
                                </Link>
                            </div>
                        </motion.div>
                    </motion.section>
                )}
            </AnimatePresence>

            <section className="container mx-auto px-4 py-20">
                <h2 className="text-3xl md:text-4xl font-bold text-center mb-16 text-gray-800">
                    <MagnifyingGlassIcon className="h-12 w-12 inline-block mr-2 text-blue-600"/>
                    어떻게 참여하나요?
                </h2>

                <div className="grid md:grid-cols-3 gap-8">
                    {[
                        {
                            icon: UserPlusIcon,
                            title: "물건 등록",
                            text: "사용하지 않는 물품을 간편하게 등록"
                        },
                        {
                            icon: MagnifyingGlassIcon,
                            title: "물건 검색",
                            text: "필요한 물건을 지도에서 찾아보기"
                        },
                        {
                            icon: ArrowPathIcon,
                            title: "대여 진행",
                            text: "직접 만나 편리하게 거래"
                        },
                    ].map((step, index) => (
                        <motion.div
                            key={index}
                            initial={{scale: 0.9}}
                            whileInView={{scale: 1}}
                            className="p-8 bg-white rounded-xl shadow-lg hover:shadow-xl transition-shadow"
                        >
                            <step.icon className="h-16 w-16 text-green-600 mb-6 mx-auto"/>
                            <div className="text-center">
                                <div className="text-2xl font-bold mb-2 text-gray-800">
                                    <span className="text-blue-600">0{index + 1}.</span> {step.title}
                                </div>
                                <p className="text-gray-600">{step.text}</p>
                            </div>
                        </motion.div>
                    ))}
                </div>
            </section>
        </motion.div>
    );
}