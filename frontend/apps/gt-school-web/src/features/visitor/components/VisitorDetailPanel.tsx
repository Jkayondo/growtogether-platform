import VisitorCard from "./VisitorCard";
import VisitorTimeline from "./VisitorTimeline";


interface VisitorTimelineEvent {

    title: string;

    description: string;

    time: string;

}


interface VisitorDetailPanelProps {

    name: string;

    purpose: string;

    host: string;

    status: string;

    badgeNumber?: string;

    arrivalTime?: string;

    duration?: string;

    timeline: VisitorTimelineEvent[];

    onCheckout?: () => void;

}


export default function VisitorDetailPanel({

    name,

    purpose,

    host,

    status,

    badgeNumber,

    arrivalTime,

    duration,

    timeline,

    onCheckout

}: VisitorDetailPanelProps) {


    return (

        <div className="gt-visitor-detail-panel">


            <VisitorCard

                name={name}

                purpose={purpose}

                host={host}

                status={status}

                badgeNumber={badgeNumber}

                arrivalTime={arrivalTime}

                duration={duration}

            />


            {
                badgeNumber && (

                    <div className="gt-visitor-badge-info">


                        <h4>
                            Badge Information
                        </h4>


                        <p>

                            <strong>
                                Badge Number:
                            </strong>

                            {" "}

                            {badgeNumber}

                        </p>


                        <p>

                            <strong>
                                Badge Status:
                            </strong>

                            {" "}

                            ISSUED

                        </p>


                    </div>

                )
            }


            <VisitorTimeline

                events={timeline}

            />


            {
                status === "CHECKED_IN" && (

                    <button

                        type="button"

                        onClick={onCheckout}

                    >
                        Checkout Visitor
                    </button>

                )
            }


        </div>

    );

}
