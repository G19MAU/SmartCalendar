import React, { useEffect, useRef, useState } from 'react';
import dayjs from 'dayjs';
import {
    Box,
    Button,
    Paper,
    TableContainer,
    Typography
} from '@mui/material';
import {
    ArrowBack as ArrowBackIcon,
    ArrowForward as ArrowForwardIcon,
    CalendarToday as CalendarTodayIcon
} from '@mui/icons-material';

import WeeklyGrid from './WeeklyGrid';
import { useCalendarContext } from '../../../../context/CalendarContext';
import isoWeek from "dayjs/plugin/isoWeek";
dayjs.extend(isoWeek)

const ROW_HEIGHT_PX = 60;
const PRE_HOURS = 3;

function WeeklyCalendar() {
    // Constant variables for dates

    const {
        weekdays,
        timeSlots,
        targetDate,
        currentTime,
        startOfWeek,
        setStartOfWeek
    } = useCalendarContext();

    // Datepicker state
    const [date, setDate] = useState(dayjs().format("YYYY-MM-DD"));
    const inputRef = useRef(null);

    const [timeColWidth, setTimeColWidth] = useState(0);
    const [dayColWidth, setDayColWidth]   = useState(0);

    const scrollRef = useRef(null);
    const timeColumnRef = useRef(null);

    const todayDate = dayjs().format("YYYY-MM-DD");
    const todayIndex = weekdays.findIndex(w => w.date === todayDate);

    // Set visible hours
    const lineTopPx =
        ROW_HEIGHT_PX + //header/weekdays height
        currentTime.hour() * ROW_HEIGHT_PX +
        (currentTime.minute() / 60) * ROW_HEIGHT_PX;
    const lineLeftPx = timeColWidth + dayColWidth * todayIndex;


    // On mount, calculate the width of the time column and day columns
    useEffect(() => {
        function updateWidths() {
            if (scrollRef.current && timeColumnRef.current) {
                const totalWidth = scrollRef.current.clientWidth;
                const tWidth = timeColumnRef.current.clientWidth;
                setTimeColWidth(tWidth);
                setDayColWidth((totalWidth - tWidth) / weekdays.length);
            }
        }

        updateWidths();
        window.addEventListener('resize', updateWidths);
        return () => window.removeEventListener('resize', updateWidths);
    }, [weekdays]);

    // On mount, scroll so current hour row is PRE_HOURS rows from the top
    const hasScrolledInitially = useRef(false);

    useEffect(() => {
        if (!hasScrolledInitially.current) {
            const nowHour = dayjs().hour();
            const index = timeSlots.findIndex(time => parseInt(time, 10) === nowHour);
            if (index > 0 && scrollRef.current) {
                const offSet = (index - PRE_HOURS) * ROW_HEIGHT_PX;
                scrollRef.current.scrollTop = offSet > 0 ? offSet : 0;
            }
            hasScrolledInitially.current = true;
        }
    }, [timeSlots]);


    const handlePrevWeek = () =>
        setStartOfWeek((prev) => prev.subtract(1, 'week'));
    const handleNextWeek = () =>
        setStartOfWeek((prev) => prev.add(1, 'week'));

    // whenever someone calls navigateToDate(date), jump the week
    useEffect(() => {
        if (targetDate) {
            setStartOfWeek(targetDate.startOf("isoWeek"));
        }
    }, [targetDate, setStartOfWeek]);

    return(
        <Box
            sx={{
                display: "flex",
                flexDirection: "column",
                flexGrow: 1,
                overflow: "hidden"
            }}
        >
            <Box
                sx={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    mb: 1,
                    mt: 1,
                    px: 1,
                }}
            >
                {/* Button changing visible week to previous */}
                <Button variant="contained"  size="small" onClick={handlePrevWeek}>
                    <ArrowBackIcon fontSize="small"/>
                </Button>

                {/* Headtitle for calender */}
                {/* Date button + hidden input native date‑picker */}
                <Box sx={{ position: "relative", display: "inline-block" }}>
                    <Button
                        variant="text"
                        size="small"
                        startIcon={<CalendarTodayIcon />}
                        sx={{
                            border: 1,
                            textTransform: "none",
                            px: 1,
                            fontSize: "1rem"
                        }}
                        onClick={() => {
                            if (inputRef.current?.showPicker) {
                                inputRef.current.showPicker();
                            } else {
                                inputRef.current.click();
                            }
                        }}
                    >
                        Vecka {startOfWeek.week()} - {startOfWeek.year()}
                    </Button>

                    <input
                        ref={inputRef}
                        type="date"
                        value={date}
                        onChange={e => {
                            const value = e.target.value;
                            if (!value) {
                             const today = dayjs().startOf("isoWeek");
                             setDate(dayjs().format("YYYY-MM-DD"));
                             setStartOfWeek(today);
                            } else {
                                setDate(value);
                                setStartOfWeek(dayjs(e.target.value).startOf("isoWeek"))
                            }
                        }}
                        style={{
                            position: "absolute",
                            top: 0,
                            left: 0,
                            width: "100%",
                            height: "100%",
                            opacity: 0,
                            pointerEvents: "none"
                        }}
                    />
                </Box>

                {/* Button changing visible week to next */}
                <Button variant ="contained" size="small" onClick={handleNextWeek}>
                    <ArrowForwardIcon fontSize="small"/>
                </Button>
            </Box>

            {/* Shows calendar */}
            <TableContainer
                component ={Paper}
                elevation="2"
                ref={scrollRef}
                sx={{
                    position: "relative",
                    height:"100%",
                    overflowY:"auto"
                }}
            >
                {/* Draw today's timeline with point + line */}
                {todayIndex >= 0 &&
                    weekdays.map((_, idx) =>
                        idx < todayIndex ? (
                            <Box
                                key={`dashed-${idx}`}
                                sx={{
                                    position: "absolute",
                                    top: `${lineTopPx}px`,
                                    left: `${timeColWidth + dayColWidth * idx}px`,
                                    width: `${dayColWidth}px`,
                                    borderTop: "1.5px dashed",
                                    borderColor: "error.main",
                                    zIndex: 2,
                                    transform: "translateY(-50%)",
                                }}
                            />
                        ) : null
                    )
                }

                {/* 2) Solida linjen + ankaret för dagens kolumn */}
                {todayIndex >= 0 && (
                    <>
                        <Box
                            sx={{
                                position: "absolute",
                                top: `${lineTopPx}px`,
                                left: `${lineLeftPx}px`,
                                width: `calc(${dayColWidth}px + 7px)`, // Ej responsiv lösning!!!!!
                                height: "2px",
                                bgcolor: "error.main",
                                zIndex: 2,
                                transform: "translateY(-50%)",
                            }}
                        />

                        {/* RÖD PRICK */}
                        <Box
                            sx={{
                                position: "absolute",
                                top: `${lineTopPx}px`,
                                left: `${lineLeftPx}px`,
                                width: 12,
                                height: 12,
                                bgcolor: "error.main",
                                borderRadius: "50%",
                                zIndex: 3,
                                transform: "translate(-50%, -50%)",
                            }}
                        />
                    </>
                )}

                <WeeklyGrid weekdays={weekdays} ref={timeColumnRef} />
            </TableContainer>
            {/* Shows error message if any */}
        </Box>
    );
}

export default WeeklyCalendar;