"use client";

import React, { useState, useCallback } from "react";
import { useRouter } from "next/navigation";
import { motion, AnimatePresence } from "framer-motion";
import { ReviewScoreIcon } from "@/components/ReviewScoreIcon";
import { fetchWithAuth } from "@/app/lib/util/fetchWithAuth";
import {ExclamationCircleIcon} from "@heroicons/react/16/solid";
import {CheckCircleIcon} from "lucide-react";

interface ReviewData {
  productScore: number | null;
  timeScore: number | null;
  kindnessScore: number | null;
  reservationId: number | null;
}

const ReviewPage: React.FC<{
  reservationId: string;
}> = ({ reservationId }) => {
  const router = useRouter();
  const [currentStep, setCurrentStep] = useState(1);
  const [reviewData, setReviewData] = useState<ReviewData>({
    productScore: 5,
    timeScore: 5,
    kindnessScore: 5,
    reservationId: parseInt(reservationId),
  });
  const [, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const BASE_URL = process.env.NEXT_PUBLIC_BASE_URL;
  
  const handleScoreChange = useCallback(
    (step: number, score: number | null) => {
      switch (step) {
        case 1:
          setReviewData((prev) => ({ ...prev, productScore: score }));
          break;
        case 2:
          setReviewData((prev) => ({ ...prev, timeScore: score }));
          break;
        case 3:
          setReviewData((prev) => ({ ...prev, kindnessScore: score }));
          break;
        default:
          break;
      }
    },
    [setReviewData]
  );

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);
    setSuccessMessage(null);

    if (
      !reviewData.productScore ||
      !reviewData.timeScore ||
      !reviewData.kindnessScore
    ) {
      setError("모든 점수를 선택해주세요.");
      setIsLoading(false);
      return;
    }

    try {
      const response = await fetchWithAuth(`${BASE_URL}/api/v1/review/create`, {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(reviewData),
      });

      if (!response) {
        setError("No response from server.");
        goToDetail();
        return;
      }
      const Data = await response.json();
      if (!response.ok) {
        setError(`${Data.msg || ""} ${response.statusText}`);
        goToDetail();
        return;
      }
      if (Data?.code !== "200-1") {
        setError(`${Data?.msg}`);
      } else {
        setSuccessMessage("리뷰가 성공적으로 작성되었습니다!");
      }
      goToDetail();
    } catch (e: any) {
      setError(` ${e.message}`);
      goToDetail();
    } finally {
      setIsLoading(false);
    }
  };

  const goToDetail = () => {
    setTimeout(() => {
      setSuccessMessage(null);
      router.push(`/mypage/reservationDetail/${reservationId}`);
    }, 3000);
  };

  const renderStepContent = (step: number) => {
    switch (step) {
      case 1:
        return (
          <StepContainer
            step={step}
            title="제품에 문제가 없었나요?"
            onNext={() => setCurrentStep(2)}
            scoreDescriptions={{
              1: "제품이 사용할 수 없는 상태였어요.",
              2: "상태가 생각보다 좋지 않았어요.",
              3: "보통 수준의 상태였어요.",
              4: "대체로 만족스러운 상태였어요.",
              5: "완벽한 상태였어요!",
            }}
            currentScore={reviewData.productScore}
            setScore={(score) => handleScoreChange(1, score)}
          >
            <ReviewScoreIcon
              score={reviewData.productScore}
              setScore={(score) => handleScoreChange(1, score)}
            />
          </StepContainer>
        );
      case 2:
        return (
          <StepContainer
            step={step}
            title="상대가 시간 약속을 잘 지켰나요?"
            onNext={() => setCurrentStep(3)}
            onPrevious={() => setCurrentStep(1)}
            scoreDescriptions={{
              1: "약속 시간을 전혀 지키지 않았어요.",
              2: "약속 시간을 잘 지키지 않았어요.",
              3: "약속 시간에 좀 늦었어요.",
              4: "약속 시간을 대체로 잘 지켰어요.",
              5: "약속 시간을 잘 지켰어요!",
            }}
            currentScore={reviewData.timeScore}
            setScore={(score) => handleScoreChange(2, score)}
          >
            <ReviewScoreIcon
              score={reviewData.timeScore}
              setScore={(score) => handleScoreChange(2, score)}
            />
          </StepContainer>
        );
      case 3:
        return (
          <StepContainer
            step={step}
            title="상대가 친절하게 응대하셨나요?"
            isFinalStep
            onPrevious={() => setCurrentStep(2)}
            onSubmit={handleSubmit}
            scoreDescriptions={{
              1: "매우 불친절했어요.",
              2: "태도가 별로 친절하지 않았어요.",
              3: "보통 수준의 친절함이었어요.",
              4: "매너 있게 대해주셨어요.",
              5: "정말 친절했어요!",
            }}
            currentScore={reviewData.kindnessScore}
            setScore={(score) => handleScoreChange(3, score)}
          >
            <ReviewScoreIcon
              score={reviewData.kindnessScore}
              setScore={(score) => handleScoreChange(3, score)}
            />
          </StepContainer>
        );
      default:
        return null;
    }
  };

  return (
      <div className="max-w-2xl mx-auto mt-12 md:mt-16 p-6 bg-gradient-to-br from-emerald-50 to-green-50 rounded-3xl shadow-lg">
        {/* 단계 진행 표시기 */}
        <div className="mb-12 relative">
          {/* 배경 라인 */}
          <div className="absolute top-1/2 left-6 right-6 h-1 bg-gray-200 transform -translate-y-1/2 rounded-full"></div>

          {/* 단계 표시 아이콘 */}
          <div className="flex items-center justify-between relative">
            {[1, 2, 3].map((step) => (
                <div key={step} className="relative z-10 flex flex-col items-center">
                  <div
                      className={`w-12 h-12 rounded-full flex items-center justify-center text-white font-medium transition-all duration-300
                    ${currentStep >= step
                          ? "bg-emerald-600 shadow-md"
                          : "bg-gray-300"}`}
                  >
                    {step}
                  </div>
                  <div className={`mt-2 text-sm font-medium ${currentStep >= step ? "text-emerald-700" : "text-gray-500"}`}>
                    {step === 1 ? "제품 상태" : step === 2 ? "시간 약속" : "친절도"}
                  </div>
                </div>
            ))}
          </div>

          {/* 진행 상태 표시 오버레이 */}
          <div
              className="absolute top-1/2 left-6 h-1 bg-emerald-500 transform -translate-y-1/2 rounded-full transition-all duration-500"
              style={{ width: `${((currentStep - 1) / 2) * 100}%` }}
          ></div>
        </div>

        {/* 알림 메시지 */}
        {successMessage && (
            <div className="bg-emerald-100 border border-emerald-200 text-gray-600 px-5 py-4 rounded-xl mb-6 flex items-start" role="alert">
              <div className="flex-shrink-0 mr-2">
                <CheckCircleIcon className="w-5 h-5 text-emerald-500" />
              </div>
              <div>
                <strong className="font-medium text-emerald-700">성공!</strong>
                <span className="ml-1">{successMessage}</span>
              </div>
            </div>
        )}

        {error && (
            <div className="bg-red-50 border border-red-200 text-gray-600 px-5 py-4 rounded-xl mb-6 flex items-start" role="alert">
              <div className="flex-shrink-0 mr-2">
                <ExclamationCircleIcon className="w-5 h-5 text-red-500" />
              </div>
              <div>
                <strong className="font-medium text-red-700">오류!</strong>
                <span className="ml-1">{error}</span>
              </div>
            </div>
        )}

        {/* 단계별 컨텐츠 애니메이션 */}
        <div className="bg-white rounded-2xl shadow-md p-6 border border-emerald-100">
          <AnimatePresence mode="wait" initial={false}>
            <motion.div
                key={currentStep}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -20 }}
                transition={{ duration: 0.3, ease: "easeInOut" }}
            >
              <form onSubmit={handleSubmit} className="space-y-6">
                {renderStepContent(currentStep)}
              </form>
            </motion.div>
          </AnimatePresence>
        </div>
      </div>
  );
};
interface StepContainerProps {
  step: number;
  title: string;
  children: React.ReactNode;
  onNext?: () => void;
  onPrevious?: () => void;
  isFinalStep?: boolean;
  onSubmit?: (e: React.FormEvent) => void;
  scoreDescriptions: {
    [score: number]: string;
  };
  currentScore: number | null;
  setScore: (score: number | null) => void;
}

const StepContainer: React.FC<StepContainerProps> = ({
  step,
  title,
  // children,
  onNext,
  onPrevious,
  isFinalStep,
  onSubmit,
  scoreDescriptions,
  currentScore,
  setScore,
}) => {
  const [selectedScore, setSelectedScore] = useState<number | null>(
    currentScore || 5
  );
  const handleScoreIconChange = (score: number | null) => {
    setSelectedScore(score);
    setScore(score);
  };

  return (
      <div className="space-y-8 bg-white p-6 rounded-xl shadow-md border border-emerald-100">
        {/* 헤더 섹션 */}
        <div className="text-center space-y-2">
          <p className="text-sm font-medium text-emerald-600">{`${step} / 3단계`}</p>
          <h2 className="text-2xl font-bold text-gray-600">{title}</h2>
        </div>

        {/* 별점 평가 섹션 */}
        <div className="flex flex-col items-center space-y-4">
          <p className="text-gray-600 mb-2">
            별점으로 상대를 평가해주세요!
          </p>

          {/* 별점 컴포넌트 */}
          <div className="bg-emerald-50 rounded-xl p-6 w-full max-w-md transition-all duration-300 hover:shadow-md">
            <div className="flex justify-center">
              <ReviewScoreIcon
                  score={selectedScore}
                  setScore={handleScoreIconChange}
              />
            </div>

            {selectedScore && (
                <div className="mt-4 text-center text-gray-600 p-3 bg-white rounded-lg border border-emerald-100">
                  {scoreDescriptions[selectedScore]}
                </div>
            )}
          </div>
        </div>

        {/* 네비게이션 버튼 */}
        <div className={`flex ${onPrevious ? 'justify-between' : 'justify-end'} mt-6`}>
          {onPrevious && (
              <button
                  type="button"
                  onClick={onPrevious}
                  className="px-5 py-2.5 border border-emerald-200 rounded-lg text-gray-600 hover:bg-emerald-50 transition-colors duration-200 flex items-center"
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-2" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M9.707 14.707a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 1.414L7.414 9H15a1 1 0 110 2H7.414l2.293 2.293a1 1 0 010 1.414z" clipRule="evenodd" />
                </svg>
                이전 단계
              </button>
          )}

          {!isFinalStep ? (
              onNext && (
                  <button
                      type="button"
                      onClick={onNext}
                      className="px-5 py-2.5 bg-emerald-600 rounded-lg text-white hover:bg-emerald-700 transition-colors duration-200 flex items-center"
                  >
                    다음 단계
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 ml-2" viewBox="0 0 20 20" fill="currentColor">
                      <path fillRule="evenodd" d="M10.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L12.586 11H5a1 1 0 110-2h7.586l-2.293-2.293a1 1 0 010-1.414z" clipRule="evenodd" />
                    </svg>
                  </button>
              )
          ) : (
              onSubmit && (
                  <button
                      type="submit"
                      onClick={onSubmit}
                      className="px-5 py-2.5 bg-emerald-600 rounded-lg text-white hover:bg-emerald-700 transition-colors duration-200 flex items-center"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-2" viewBox="0 0 20 20" fill="currentColor">
                      <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                    </svg>
                    리뷰 제출
                  </button>
              )
          )}
        </div>
      </div>
  );
};

export default ReviewPage;
