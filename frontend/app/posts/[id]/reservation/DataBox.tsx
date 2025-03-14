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
    <div className="p-4 border rounded-md flex flex-col items-center">
      <div>{moment(date).format("YYYY-MM-DD")}</div>
      <div className="mt-2">{time}</div>
      <div className="flex flex-col mt-2">
        <div className="flex">
          {timeBlocks.slice(0, 12).map((timeBlock) => (
            <button
              key={timeBlock}
              className={`w-10 h-6 m-0.5 border rounded-sm text-xs whitespace-nowrap flex items-center justify-center ${
                isTimeReserved(timeBlock)
                  ? "bg-red-200 text-gray-500 cursor-not-allowed"
                  : time === timeBlock
                  ? "bg-green-200"
                  : "hover:bg-gray-100"
              }`}
              onClick={() => handleTimeBlockClick(timeBlock)}
              disabled={isTimeReserved(timeBlock)} // 예약된 시간은 클릭 비활성화
            >
              {timeBlock}
            </button>
          ))}
        </div>
        <div className="flex">
          {timeBlocks.slice(12).map((timeBlock) => (
            <button
              key={timeBlock}
              className={`w-10 h-6 m-0.5 border rounded-sm text-xs whitespace-nowrap flex items-center justify-center ${
                isTimeReserved(timeBlock)
                  ? "bg-red-200 text-gray-500 cursor-not-allowed"
                  : time === timeBlock
                  ? "bg-green-200"
                  : "hover:bg-gray-100"
              }`}
              onClick={() => handleTimeBlockClick(timeBlock)}
              disabled={isTimeReserved(timeBlock)} // 예약된 시간은 클릭 비활성화
            >
              {timeBlock}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};

export default DateBox;
