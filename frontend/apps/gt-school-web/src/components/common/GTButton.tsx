interface GTButtonProps {
  children: React.ReactNode;
}

export default function GTButton({ children }: GTButtonProps) {
  return (
    <button className="gt-button">
      {children}
    </button>
  );
}
