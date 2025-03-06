import moment from "moment";
import { useState } from "react";

interface DateBoxProps {
  date: Date | null;
  onTimeChange: (time: string) => void;
  time: string;
}

const DateBox: React.FC<DateBoxProps> = ({ date, onTimeChange, time }) => {
  if (!date) {
    return (
      <div className="p-4 border rounded-md w-64 h-32 flex items-center justify-center">
        날짜를 선택하세요.
      </div>
    );
  }

  return (
    <div className="p-4 border rounded-md w-64 h-32 flex flex-col justify-between items-center">
      <div>{moment(date).format("YYYY-MM-DD")}</div>
      <input
        type="time"
        value={time}
        onChange={(e) => onTimeChange(e.target.value)}
        className="border rounded-md p-2 w-full"
      />
    </div>
  );
};

export default DateBox;
