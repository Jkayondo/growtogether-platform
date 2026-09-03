interface VisitorTimelineEvent {

    title: string;

    description: string;

    time: string;

}


interface VisitorTimelineProps {

    events: VisitorTimelineEvent[];

}


export default function VisitorTimeline({

    events

}: VisitorTimelineProps) {


    return (

        <div className="gt-visitor-timeline">


            {
                events.map((event, index) => (

                    <div
                        key={index}
                        className="gt-visitor-timeline-item"
                    >


                        <div className="gt-visitor-timeline-marker">
                            ●
                        </div>


                        <div className="gt-visitor-timeline-content">


                            <h4>
                                {event.title}
                            </h4>


                            <p>
                                {event.description}
                            </p>


                            <span>
                                {event.time}
                            </span>


                        </div>


                    </div>

                ))
            }


        </div>

    );

}
