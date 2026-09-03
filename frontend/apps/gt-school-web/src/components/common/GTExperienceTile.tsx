interface GTExperienceTileProps {
  icon: React.ElementType;
  title: string;
}

export default function GTExperienceTile({
  icon: Icon,
  title,
}: GTExperienceTileProps) {
  return (
    <div className="gt-experience-tile">

      <div className="gt-experience-icon">
        <Icon color="currentColor" />
      </div>

      <div className="gt-experience-title">
        {title}
      </div>

    </div>
  );
}
