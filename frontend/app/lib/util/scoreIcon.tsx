interface Props {
  className: string;
  score: number;
  size?: number;
  round?: boolean;
}

export default function ScoreIcon({ className, score, size, round }: Props) {
  const getLevel = (score: number): string => {
    if (score < 20) {
      return "One";
    } else if (score < 40) {
      return "Two";
    } else if (score < 60) {
      return "Three";
    } else if (score < 80) {
      return "Four";
    } else {
      return "Five";
    }
  };

  const getLevelIconUrl = (level: string): string => {
    return `/levelicon/level${level}.png`;
  };

  const level = getLevel(score);
  const imageUrl = getLevelIconUrl(level);

  return <span className={`${className} ${round ? "rounded-full overflow-hidden" : ""}`}>
  <img src={imageUrl} width={size || 60} height={size || 60} alt={`Level ${level} Icon`} />
  </span>;
}
