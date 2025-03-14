"use client";

import React, { useState, useCallback } from "react";
import { useRouter } from "next/navigation";
import { motion, AnimatePresence } from "framer-motion";
import { ReviewScoreIcon } from "@/components/ReviewScoreIcon";
import { fetchWithAuth } from "@/app/lib/util/fetchWithAuth";

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

  const fetchHelper = async (url: string, options?: RequestInit) => {
    const accessToken = sessionStorage.getItem("access_token");
    if (accessToken) {
      return fetchWithAuth(url, options);
    } else {
      return fetch(url, options);
    }
  };

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
      const response = await fetchHelper(`${BASE_URL}/api/v1/review/create`, {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(reviewData),
      });

      const Data = await response.json();
      if (!response.ok) {
        setError(`${Data.msg || ""} ${response.statusText}`);
        goToDetail();
        return;
      }
      if (Data?.code.startsWith("403")) {
        router.push("/login");
      }
      if (Data?.code !== "200-1") {
        setError(`${Data?.msg}`);
      } else {
        setSuccessMessage("리뷰가 성공적으로 작성되었습니다!");
      }
      goToDetail();
    } catch (e: any) {
      if (e.status === 403) {
        router.push("/login");
      }
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
    <div className="max-w-2xl mx-auto p-6 space-y-8">
      <div className="flex items-center justify-center relative mb-12">
        <div className="flex items-center justify-between w-64">
          {[1, 2, 3].map((step) => (
            <div key={step} className="relative z-10">
              <div
                className={`w-10 h-10 rounded-full flex items-center justify-center text-white
                                    ${
                                      currentStep >= step
                                        ? "bg-green-500"
                                        : "bg-gray-300"
                                    }`}
              >
                {step}
              </div>
            </div>
          ))}
        </div>
        <div className="absolute top-1/2 left-16 right-16 h-1 bg-gray-200 transform -translate-y-1/2"></div>
      </div>

      {successMessage && (
        <div
          className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded relative mb-4"
          role="alert"
        >
          <strong className="font-bold">성공!</strong>
          <span className="block sm:inline"> {successMessage}</span>
        </div>
      )}
      {error && (
        <div
          className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4 text-center"
          role="alert"
        >
          <strong className="font-bold">오류!</strong>
          <span className="block sm:inline"> {error}</span>
        </div>
      )}

      <AnimatePresence mode="wait" initial={false}>
        <motion.div
          key={currentStep}
          initial={{ opacity: 0, x: 50 }}
          animate={{ opacity: 1, x: 0 }}
          exit={{ opacity: 0, x: -50 }}
          transition={{ duration: 0.3 }}
        >
          <form onSubmit={handleSubmit} className="space-y-6">
            {renderStepContent(currentStep)}
          </form>
        </motion.div>
      </AnimatePresence>
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
  children,
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
    <div className="space-y-6">
      <div className="mb-6">
        <h3 className="mb-4 text-xl font-bold text-gray-800 text-center">{`${step} / 3단계`}</h3>
        <h1 className="text-2xl font-bold text-gray-800 text-center">
          {title}
        </h1>
      </div>
      <div className="flex flex-col items-center justify-center mb-8">
        <p className="text-sm text-gray-500 mb-2 text-center mb-8">
          별점으로 상대를 평가해주세요!
        </p>
        <ReviewScoreIcon
          score={selectedScore}
          setScore={handleScoreIconChange}
        />
        <div className="text-sm text-gray-600 text-center mt-2">
          {selectedScore && scoreDescriptions[selectedScore]}
        </div>
      </div>
      <div
        className={`flex ${!onPrevious ? "justify-end" : "justify-between"}`}
      >
        {onPrevious && (
          <button
            type="button"
            onClick={onPrevious}
            className="px-4 py-2 border rounded-md text-gray-700 hover:bg-gray-100"
          >
            이전 단계
          </button>
        )}
        {!isFinalStep
          ? onNext && (
              <button
                type="button"
                onClick={onNext}
                className="px-6 py-2 font-semibold text-white bg-green-500 rounded-md hover:bg-green-600 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-opacity-50"
              >
                다음 단계
              </button>
            )
          : onSubmit && (
              <button
                type="submit"
                className="px-6 py-2 font-semibold text-white bg-green-500 rounded-md hover:bg-green-600 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-opacity-50"
                onClick={onSubmit}
              >
                리뷰 제출
              </button>
            )}
      </div>
    </div>
  );
};

export default ReviewPage;
