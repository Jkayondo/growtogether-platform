import VisitorStatusBadge from "./VisitorStatusBadge";


interface VisitorCardProps {

    name: string;

    purpose: string;

    host: string;

    status: string;

    badgeNumber?: string;

    arrivalTime?: string;

    duration?: string;

}


export default function VisitorCard({

    name,

    purpose,

    host,

    status,

    badgeNumber,

    arrivalTime,

    duration

}: VisitorCardProps) {


    return (

        <div className="gt-visitor-card">


            <div className="gt-visitor-card-header">

                <h3>
                    {name}
                </h3>

                <VisitorStatusBadge
                    status={status}
                />

            </div>


            <div className="gt-visitor-card-body">


                <p>
                    <strong>Purpose:</strong>
                    {" "}
                    {purpose}
                </p>


                <p>
                    <strong>Host:</strong>
                    {" "}
                    {host}
                </p>


                {
                    badgeNumber && (
                        <p>
                            <strong>Badge:</strong>
                            {" "}
                            {badgeNumber}
                        </p>
                    )
                }


                {
                    arrivalTime && (
                        <p>
                            <strong>Arrival:</strong>
                            {" "}
                            {arrivalTime}
                        </p>
                    )
                }


                {
                    duration && (
                        <p>
                            <strong>Duration:</strong>
                            {" "}
                            {duration}
                        </p>
                    )
                }


            </div>


        </div>

    );

}
