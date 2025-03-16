"use client";
import moment from "moment";

interface DateBoxProps {
  date: Date | null;
  onTimeChange: (time: string) => void;
  time: string;
  events: any[];
}

const DateBox: React.FC<DateBoxProps> = ({
  date,
  onTimeChange,
  time,
  events,
}) => {
  if (!date) {
    return (
      <div className="p-4 border rounded-md w-64 h-32 flex items-center justify-center">
        날짜를 선택하세요.
      </div>
    );
  }

  const timeBlocks = Array.from({ length: 24 }, (_, i) => {
    // 24개 블록으로 변경
    return `${i.toString().padStart(2, "0")}:00`;
  });

  const isTimeReserved = (timeBlock: string) => {
    if (!events) return false;
    const dateString = moment(date).format("YYYY-MM-DD");
    const timeBlockDateTime = moment(`${dateString}T${timeBlock}`);

    return events.some((event) => {
      const startDate = moment(event.start).startOf("day");
      const endDate = moment(event.end).startOf("day");
      const currentDate = moment(date).startOf("day");

      if (
        currentDate.isSameOrAfter(startDate) &&
        currentDate.isSameOrBefore(endDate)
      ) {
        const startTime = moment(event.start);
        const endTime = moment(event.end);

        if (
          currentDate.isSame(startDate) &&
          timeBlockDateTime.isBefore(startTime)
        ) {
          return false;
        }

        if (
          currentDate.isSame(endDate) &&
          timeBlockDateTime.isSameOrAfter(endTime)
        ) {
          return false;
        }

        return true;
      }
      return false;
    });
  };

  const handleTimeBlockClick = (timeBlock: string) => {
    if (!isTimeReserved(timeBlock)) {
      onTimeChange(timeBlock);
    } else {
      alert("선택하신 시간은 이미 예약되었습니다.");
    }
  };

  return (
      <div className="p-6 bg-green-50 rounded-xl border-2 border-green-200 shadow-sm w-full max-w-2xl">
        {/* 날짜 표시 섹션 */}
        <div className="mb-6 text-center">
          <div className="text-2xl font-semibold text-gray-600">
            {moment(date).format("YYYY년 MM월 DD일")}
          </div>
          <div className="mt-2 text-lg text-gray-600/90">현재 선택 시간: {time || '--:--'}</div>
        </div>

        {/* 시간 블록 그리드 */}
        <div className="grid grid-cols-2 gap-4">
          {/* 오전 시간대 */}
          <div className="space-y-3">
            <div className="text-gray-600 font-medium ml-1">오전</div>
            <div className="grid grid-cols-3 gap-2">
              {timeBlocks.slice(0, 12).map((timeBlock) => (
                  <button
                      key={timeBlock}
                      disabled={isTimeReserved(timeBlock)}
                      className={`px-3 py-2 rounded-lg border-2 transition-all duration-200
              ${isTimeReserved(timeBlock)
                          ? 'bg-green-500/20 border-green-400 text-gray-600/50 cursor-not-allowed'
                          : time === timeBlock
                              ? 'bg-green-400 border-green-600 text-gray-700 shadow-md'
                              : 'bg-white border-green-200 text-gray-600 hover:bg-green-100 hover:border-green-300'}
            }`}
                      onClick={() => handleTimeBlockClick(timeBlock)}
                  >
                    {timeBlock}
                  </button>
              ))}
            </div>
          </div>

          {/* 오후 시간대 */}
          <div className="space-y-3">
            <div className="text-gray-600 font-medium ml-1">오후</div>
            <div className="grid grid-cols-3 gap-2">
              {timeBlocks.slice(12).map((timeBlock) => (
                  <button
                      key={timeBlock}
                      className={`px-3 py-2 rounded-lg border-2 transition-all duration-200
              ${isTimeReserved(timeBlock)
                          ? 'bg-green-500/20 border-green-400 text-gray-600/50 cursor-not-allowed'
                          : time === timeBlock
                              ? 'bg-green-400 border-green-600 text-gray-700 shadow-md'
                              : 'bg-white border-green-200 text-gray-600 hover:bg-green-100 hover:border-green-300'}
            }`}
                      onClick={() => handleTimeBlockClick(timeBlock)}
                      disabled={isTimeReserved(timeBlock)}
                  >
                    {timeBlock}
                  </button>
              ))}
            </div>
          </div>
        </div>

        {/* 안내 메시지 */}
        <div className="mt-6 text-sm text-gray-600/90 text-center">
          <p>✅ 예약 가능 시간을 클릭해주세요</p>
          <p className="mt-1">⛔️ 빨간색 표시는 이미 예약된 시간입니다</p>
        </div>
      </div>
  );
};

export default DateBox;
