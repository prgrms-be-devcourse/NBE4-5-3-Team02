"use client";
import { useState } from 'react';
import { motion, AnimatePresence } from "framer-motion";
import { ArrowPathIcon, HeartIcon, GlobeAltIcon, SparklesIcon, MagnifyingGlassIcon, UserPlusIcon } from "@heroicons/react/24/outline";

export default function MainPage() {
  const [showBenefits, setShowBenefits] = useState(false);

  return (
      <div className="min-h-screen bg-gradient-to-b from-green-50 to-blue-50">

        <motion.section
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            className="container mx-auto px-4 py-20 text-center"
        >
            <motion.h1
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 1 }}
                className="text-4xl md:text-6xl font-bold mb-6 text-green-800 flex items-center justify-center">
                <SparklesIcon className="h-10 w-10 text-yellow-400" />
                소유 경제의 종말
            </motion.h1>

          <motion.div
              initial={{ y: 20, opacity: 0 }}
              animate={{ y: 0, opacity: 1 }}
              className="space-y-8"
          >
            <p className="text-xl md:text-2xl text-gray-600">
              소유에서 공유로의 전환은 단순한 선택이 아닌<br />
              <span className="text-green-600 font-semibold">미래 세대를 위한 필수 의무입니다</span>
            </p>

            <motion.button
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                onClick={() => setShowBenefits(!showBenefits)}
                className="bg-blue-600 text-white px-8 py-4 rounded-full text-lg font-semibold shadow-lg hover:shadow-xl transition-all"
            >
              {showBenefits ? '기본 화면 보기' : '왜 공유인가요? →'}
            </motion.button>
          </motion.div>
        </motion.section>

        <AnimatePresence>
          {showBenefits && (
              <motion.section
                  initial={{ opacity: 0, y: 50 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -50 }}
                  className="container mx-auto px-4 py-16"
              >
                <div className="grid md:grid-cols-3 gap-8 mb-12">
                  {[
                    {
                      icon: ArrowPathIcon,
                      title: "경제적 가치 창출",
                      value: "₩720,000+",
                      desc: "연간 최대 절감액",
                      color: "bg-blue-100"
                    },
                    {
                      icon: HeartIcon,
                      title: "사회적 관계 증진",
                      value: "40%↑",
                      desc: "커뮤니티 신뢰도 증가",
                      color: "bg-pink-100"
                    },
                    {
                      icon: GlobeAltIcon,
                      title: "환경적 영향",
                      value: "18kg↓",
                      desc: "CO₂ 배출 감소량",
                      color: "bg-green-100"
                    }
                  ].map((benefit, idx) => (
                      <motion.div
                          key={idx}
                          className={`p-8 rounded-2xl ${benefit.color} transition-all duration-300 hover:shadow-lg`}
                      >
                          <benefit.icon className="h-16 w-16 mb-6 mx-auto text-blue-600" />

                          <h3 className="text-2xl font-bold mb-4 text-gray-900">
                              {benefit.title}
                          </h3>

                          <div className="text-4xl font-bold mb-2 text-gray-900">
                              {benefit.value}
                          </div>

                          <p className="text-gray-800">
                              {benefit.desc}
                          </p>
                      </motion.div>
                  ))}
                </div>

                <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    className="text-center bg-white p-8 rounded-2xl shadow-xl"
                >
                  <h2 className="text-3xl font-bold mb-6">
                    <UserPlusIcon className="h-12 w-12 inline-block mr-2 text-green-600" />
                    지금 바로 시작하세요!
                  </h2>
                  <p className="text-xl mb-8 text-gray-800">
                    더 이상 물건을 쌓아두지 마시고,<br />
                    진정한 공유 경제의 일원이 되어보세요
                  </p>
                  <div className="space-x-4">
                    <motion.button
                        whileHover={{ scale: 1.05 }}
                        className="bg-green-600 text-white px-8 py-4 rounded-full text-lg font-bold shadow-lg"
                    >
                      회원가입
                    </motion.button>
                    <motion.button
                        whileHover={{ scale: 1.05 }}
                        className="bg-blue-600 text-white px-8 py-4 rounded-full text-lg font-bold shadow-lg"
                    >
                      상세 설명 보기 →
                    </motion.button>
                  </div>
                </motion.div>
              </motion.section>
          )}
        </AnimatePresence>

        <section className="container mx-auto px-4 py-20">
          <h2 className="text-3xl md:text-4xl font-bold text-center mb-16 text-gray-800">
            <MagnifyingGlassIcon className="h-12 w-12 inline-block mr-2 text-blue-600" />
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
                    initial={{ scale: 0.9 }}
                    whileInView={{ scale: 1 }}
                    className="p-8 bg-white rounded-xl shadow-lg hover:shadow-xl transition-shadow"
                >
                  <step.icon className="h-16 w-16 text-green-600 mb-6 mx-auto" />
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

        <motion.section
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            className="bg-green-800 text-white py-20"
        >
          <div className="container mx-auto px-4 text-center">
            <h2 className="text-3xl md:text-4xl font-bold mb-8">
              🌱 지금 바로 시작하세요!
            </h2>
            <p className="text-xl mb-8">
              더 이상 물건을 쌓아두지 마시고,<br />
              지속 가능한 공유 경제에 동참해보세요
            </p>
            <motion.button
                whileHover={{ scale: 1.05 }}
                className="bg-white text-green-800 px-8 py-4 rounded-full text-lg font-bold shadow-lg flex items-center mx-auto"
            >
              <UserPlusIcon className="h-6 w-6 mr-2" />
              회원가입
            </motion.button>
          </div>
        </motion.section>
      </div>
  );
}