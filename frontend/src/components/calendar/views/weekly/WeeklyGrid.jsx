import {Divider, Table, TableBody, TableCell, TableHead, TableRow, Typography} from "@mui/material";
import dayjs from "dayjs";
import { forwardRef } from "react";

import WeeklyActivityBox from "./WeeklyActivityBox";
import {useCalendarContext} from "../../../../context/CalendarContext";


const WeeklyGrid = forwardRef(({ weekdays= [] }, timeColumnRef) => {
    const {
        filteredActivities,
        categories,
        handleCellClick,
        timeSlots,
        handleActivityClick,
    } = useCalendarContext();

    return (
        <Table stickyHeader>
            {/* TableHead for all the weekdays */}
            <TableHead>
                <TableRow>
                    {/* Empty corner cell */}
                    <TableCell
                        ref={timeColumnRef}
                        sx={{
                            position: 'sticky',
                            top: 0,
                            backgroundColor: 'background.paper',
                            zIndex: 2,
                            borderRight: '1px solid rgba(224,224,224,1)'
                        }}
                    />

                    {/* Weekday headers */}
                    {weekdays.map((day) => {
                        const isToday = dayjs(day.date).isSame(dayjs(), "day");
                        return (
                            <TableCell
                                key={day.date}
                                align="center"
                                sx={{
                                    position: 'sticky',
                                    top: 0,
                                    backgroundColor: isToday ? 'lightblue' : 'background.paper',
                                    zIndex: 2,
                                    borderRight: '1px solid rgba(224,224,224,1)'
                                }}
                            >
                                <Typography variant="subtitle1">
                                    {day.name} {dayjs(day.date).format("DD/MM")}
                                </Typography>
                            </TableCell>
                        );
                    })}
                </TableRow>
            </TableHead>

            {/* TableBody for the time and cells */}
            <TableBody>
                {/* Maps out times from 00:00 - 23:00 for every row in the first column */ }
                {timeSlots.map((time) => (
                    <TableRow key={time} sx={{ height:"60px" }}>
                        <TableCell sx={{  borderRight:"1px solid #ccc", padding:"15px"}}>
                            {time}
                        </TableCell>


                        {weekdays.map((day, idx) => {
                            // Filters and sets every date & time for each
                            const cellDate = dayjs(day.date);
                            const now = dayjs();
                            const cellStart = dayjs(`1970-01-01T${time}`);
                            const cellEnd = cellStart.add(1, "hour");
                            const cellHour = cellStart.hour();
                            const isPast = cellDate.isBefore(now, "day") ||
                                (cellDate.isSame(now, "day") && cellHour < now.hour());


                            const hits = filteredActivities.filter((a) => {
                                const activityDate = dayjs(a.date).format("YYYY-MM-DD");
                                const start = dayjs(`1970-01-01T${a.startTime}`);
                                const end = start.add(1, "minute")

                                return (
                                    activityDate === day.date && 
                                    end.isAfter(cellStart) &&
                                    start.isBefore(cellEnd)
                                );
                            });
                            return(
                                    <TableCell
                                        key={`${day.name}-${time}`}
                                        sx={{
                                            position:"relative",
                                            padding:"0",
                                            cursor: "pointer",
                                            backgroundColor: isPast ? 'grey.200' : "inherit",
                                            borderLeft: idx > 0 ? "1px solid #ccc" : "none",
                                            "&:hover": {
                                                backgroundColor: isPast ? "grey.300" : "grey.100",
                                            },
                                        }}
                                        onClick={() => {
                                            if (hits.length === 0) {
                                                handleCellClick(day.date, time);
                                            }

                                        }}
                                    >
                                        {hits.length>0 && (
                                            <WeeklyActivityBox
                                                filteredActivities={hits}
                                                onClick={(e) => handleActivityClick(e, hits[0], idx)} // Pass the first activity to the click handler
                                                categories={categories}
                                            />
                                        )}
                                    </TableCell>
                                );
                        })}
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
});

export default WeeklyGrid;