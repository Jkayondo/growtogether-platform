import { useEffect, useState } from "react";

import {
    getVisitorCheckIns
} from "../../../services/visitorApi";

import VisitorDetailPanel
    from "./VisitorDetailPanel";


interface VisitorCheckIn {

    id: string;

    visitorName: string;

    purpose: string;

    status: string;

    badgeNumber?: string;

    arrivalTime?: string;

}



export default function ActiveVisitorsPanel() {


    const [visitors, setVisitors] =
        useState<VisitorCheckIn[]>([]);



    const [loading, setLoading] =
        useState(true);



    const [error, setError] =
        useState<string | null>(null);


    const [selectedVisitor, setSelectedVisitor] =
        useState<VisitorCheckIn | null>(null);




    useEffect(() => {


        getVisitorCheckIns()

            .then((data) => {


                const activeVisitors =
                    data.filter(
                        (visitor) =>
                            visitor.status === "ACTIVE"
                    );


                setVisitors(
                    activeVisitors
                );


            })

            .catch((err) => {


                setError(
                    err.message
                );


            })

            .finally(() => {


                setLoading(false);


            });



    }, []);





    if (loading) {

        return (

            <div className="gt-card">

                Loading active visitors...

            </div>

        );

    }





    if (error) {

        return (

            <div className="gt-card">

                Unable to load active visitors.

            </div>

        );

    }





    return (

        <div className="gt-card">


            <h3>

                Visitors Currently Inside

            </h3>



            {
                visitors.length === 0 && (

                    <p>
                        No active visitors.
                    </p>

                )
            }




            {
                visitors.map(

                    (visitor) => (

                        <div

                            key={visitor.id}

                            className="gt-activity-item"

                        >

                            <strong>

                                {visitor.visitorName}

                            </strong>


                            <p>

                                Purpose:
                                {" "}
                                {visitor.purpose}

                            </p>


                            <small>

                                Status:
                                {" "}
                                {visitor.status}

                            </small>


                            <button

                                type="button"

                                onClick={() =>
                                    setSelectedVisitor(visitor)
                                }

                            >

                                View Details

                            </button>


                        </div>

                    )

                )
            }


            {
                selectedVisitor && (

                    <VisitorDetailPanel

                        name={selectedVisitor.visitorName}

                        purpose={selectedVisitor.purpose}

                        host=""

                        status="CHECKED_IN"

                        badgeNumber={
                            selectedVisitor.badgeNumber
                        }

                        arrivalTime={
                            selectedVisitor.arrivalTime
                        }

                        timeline={[
                            {
                                title: "Checked In",
                                description:
                                    "Visitor entered campus",
                                time:
                                    selectedVisitor.arrivalTime ?? ""
                            }
                        ]}

                        onCheckout={() =>
                            setSelectedVisitor(null)
                        }

                    />

                )
            }


        </div>

    );

}