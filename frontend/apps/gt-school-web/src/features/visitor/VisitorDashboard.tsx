import { useEffect, useState } from "react";

import GTSection from "../../components/common/GTSection";

import VisitorSummaryCards from "./components/VisitorSummaryCards";

import VisitorCheckInForm
    from "./components/VisitorCheckInForm";

import ActiveVisitorsPanel
    from "./components/ActiveVisitorsPanel";

import VisitorBadgePanel
    from "./components/VisitorBadgePanel";

import { getVisitorSummary } from "../../services/visitorService";


export default function VisitorDashboard() {


    const [summary, setSummary] =
        useState<any>(null);


    const [loading, setLoading] =
        useState(true);


    const [error, setError] =
        useState<string | null>(null);



    useEffect(() => {


        getVisitorSummary()

            .then((data) => {

                setSummary(data);

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

            <div className="gt-dashboard">

                <div className="gt-card">

                    Loading visitor dashboard...

                </div>

            </div>

        );

    }




    if (error || !summary) {

        return (

            <div className="gt-dashboard">

                <div className="gt-card">

                    Unable to load visitor management.

                </div>

            </div>

        );

    }




    return (

        <div className="gt-dashboard">


            <GTSection title="Visitor Management">


                <VisitorSummaryCards

                    totalRequests={
                        summary.totalRequests
                    }

                    activeVisitors={
                        summary.activeVisitors
                    }

                    activeBadges={
                        summary.activeBadges
                    }

                />



                <div className="gt-visitor-operation-grid">


                    <VisitorCheckInForm />


                    <ActiveVisitorsPanel />


                    <VisitorBadgePanel />


                </div>


            </GTSection>


        </div>

    );

}