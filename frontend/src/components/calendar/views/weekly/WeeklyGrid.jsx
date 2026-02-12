import {Divider, Table, TableBody, TableCell, TableHead, TableRow, Typography} from "@mui/material";
import dayjs from "dayjs";
import { forwardRef, useState } from "react";

import WeeklyActivityBox from "./WeeklyActivityBox";
import {useCalendarContext} from "../../../../context/CalendarContext";


const WeeklyGrid = forwardRef(({ weekdays= [] }, timeColumnRef) => {
    const {
        filteredActivities,
        categories,
        handleCellClick,
        timeSlots,
        handleActivityClick,
        createOrUpdateActivity
    } = useCalendarContext();

    const [dragOverKey, setDragOverKey] = useState(null);

    const handleDrop = async (e) => {
        e.preventDefault();
        const data = e.dataTransfer.getData('text/plain');
        const payload = data ? JSON.parse(data) : null;
        const id = payload?.id;
        const start = payload?.startTime;
        const end = payload?.endTime;
        const date = e.currentTarget.dataset.date;
        const time = e.currentTarget.dataset.time;

        if (!id || !date || !time) return;

        const activity = filteredActivities.find(a => a.id === id);
        if (!activity) return;

        const duration = dayjs(`1970-01-01T${end}`).diff(dayjs(`1970-01-01T${start}`), 'minute');
        const newStart = dayjs(`1970-01-01T${time}`);
        const newEnd = newStart.add(duration, 'minute');

        try {
            await createOrUpdateActivity({
                ...activity,
                id,
                date,
                startTime: newStart.format('HH:mm'),
                endTime: newEnd.format('HH:mm')
            }, 'edit');
        } catch (err) {
            console.error('Failed to move activity', err);
        }
        setDragOverKey(null);
    };

    const handleDragEnter = (e) => {
        const key = e.currentTarget.dataset.key;
        setDragOverKey(key);
    };

    const handleDragLeave = (e) => {
        const key = e.currentTarget.dataset.key;
        if (dragOverKey === key) {
            setDragOverKey(null);
        }
    };

    const formatCellKey = (dateStr, timeStr) => {
        const date = dayjs(dateStr).format("YYYY-MM-DD");
        const time = dayjs(`1970-01-01T${timeStr}`).format("HH:mm");
        return `${date}-${time}`;
    };

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
                            const cellKey = formatCellKey(day.date, time);
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
                                const end = start.add(1, "minute");

                                return (
                                    activityDate === day.date && 
                                    end.isAfter(cellStart) &&
                                    start.isBefore(cellEnd)
                                );
                            });

                            return (
                                <TableCell
                                    key={cellKey}
                                    data-key={cellKey}
                                    data-date={day.date}
                                    data-time={time}
                                    tabIndex={0}
                                    onDragOver={(e) => {
                                        e.preventDefault();
                                        e.dataTransfer.dropEffect = 'move';
                                    }}
                                    onDragEnter={handleDragEnter}
                                    onDragLeave={handleDragLeave}
                                    onDrop={handleDrop}
                                    sx={{
                                        pointerEvents: "all",
                                        position: "relative",
                                        padding: "0",
                                        cursor: "pointer",
                                        borderLeft: idx > 0 ? "1px solid #ccc" : "none",

                                        //Color change on hover bugs
                                        backgroundColor:
                                            dragOverKey === cellKey
                                                ? (isPast ? 'grey.300' : 'grey.100')
                                                : (isPast ? 'grey.200' : 'inherit'),
                                        "&:hover": {
                                                backgroundColor: dragOverKey !== cellKey
                                                    ? (isPast ? 'grey.300' : 'grey.100')
                                                    : undefined
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