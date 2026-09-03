import VisitorStatusBadge from "./VisitorStatusBadge";


interface VisitorApprovalCardProps {

    name: string;

    purpose: string;

    host: string;

    requestedTime: string;

    status: string;

    onApprove?: () => void;

    onReject?: () => void;

}


export default function VisitorApprovalCard({

    name,

    purpose,

    host,

    requestedTime,

    status,

    onApprove,

    onReject

}: VisitorApprovalCardProps) {


    return (

        <div className="gt-visitor-approval-card">


            <div className="gt-visitor-card-header">

                <h3>
                    Visitor Request
                </h3>

                <VisitorStatusBadge
                    status={status}
                />

            </div>


            <div className="gt-visitor-card-body">


                <p>
                    <strong>Name:</strong>
                    {" "}
                    {name}
                </p>


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


                <p>
                    <strong>Requested:</strong>
                    {" "}
                    {requestedTime}
                </p>


            </div>


            <div className="gt-visitor-card-actions">


                <button
                    type="button"
                    onClick={onApprove}
                >
                    Approve
                </button>


                <button
                    type="button"
                    onClick={onReject}
                >
                    Reject
                </button>


            </div>


        </div>

    );

}
