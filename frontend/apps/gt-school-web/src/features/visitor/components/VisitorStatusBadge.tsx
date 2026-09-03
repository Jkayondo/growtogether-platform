interface VisitorStatusBadgeProps {

    status: string;

}


export default function VisitorStatusBadge({

    status

}: VisitorStatusBadgeProps) {


    const label =
        status
            .replaceAll("_", " ");


    return (

        <span className={`gt-visitor-status ${status.toLowerCase()}`}>

            {label}

        </span>

    );

}
