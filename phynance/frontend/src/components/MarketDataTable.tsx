import React, { useMemo, useState, useEffect } from "react";
import {
  Box,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TableSortLabel,
  TablePagination,
  Paper,
  TextField,
  IconButton,
  Toolbar,
  Typography,
  Skeleton,
  Button,
  useMediaQuery,
} from "@mui/material";
import {
  Download as DownloadIcon,
  Search as SearchIcon,
} from "@mui/icons-material";
import { MarketDataDto } from "../types/MarketDataDto";
import { formatCurrency } from "../utils/format";

export interface MarketDataTableProps {
  data: MarketDataDto[];
  loading?: boolean;
  onRefresh?: () => void;
}

type Order = "asc" | "desc";

type NumericKeys = "open" | "high" | "low" | "close" | "volume";

const columns = [
  { id: "symbol", label: "Symbol", minWidth: 80 },
  { id: "timestamp", label: "Timestamp", minWidth: 120 },
  { id: "open", label: "Open", minWidth: 80, numeric: true },
  { id: "high", label: "High", minWidth: 80, numeric: true },
  { id: "low", label: "Low", minWidth: 80, numeric: true },
  { id: "close", label: "Close", minWidth: 80, numeric: true },
  { id: "volume", label: "Volume", minWidth: 100, numeric: true },
];

function descendingComparator<T>(a: T, b: T, orderBy: keyof T) {
  if (b[orderBy] < a[orderBy]) return -1;
  if (b[orderBy] > a[orderBy]) return 1;
  return 0;
}

function getComparator<Key extends keyof any>(
  order: Order,
  orderBy: Key
): (a: { [key in Key]: any }, b: { [key in Key]: any }) => number {
  return order === "desc"
    ? (a, b) => descendingComparator(a, b, orderBy)
    : (a, b) => -descendingComparator(a, b, orderBy);
}

function stableSort<T>(array: T[], comparator: (a: T, b: T) => number) {
  const stabilized = array.map((el, idx) => [el, idx] as [T, number]);
  stabilized.sort((a, b) => {
    const cmp = comparator(a[0], b[0]);
    if (cmp !== 0) return cmp;
    return a[1] - b[1];
  });
  return stabilized.map((el) => el[0]);
}

function exportToCSV(data: MarketDataDto[]) {
  const header = columns.map((col) => col.label).join(",");
  const rows = data.map((row) =>
    [
      row.symbol,
      row.timestamp,
      row.open,
      row.high,
      row.low,
      row.close,
      row.volume,
    ].join(",")
  );
  const csv = [header, ...rows].join("\n");
  const blob = new Blob([csv], { type: "text/csv" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = "market_data.csv";
  a.click();
  URL.revokeObjectURL(url);
}

const MarketDataTable: React.FC<MarketDataTableProps> = ({
  data,
  loading,
  onRefresh,
}) => {
  const [order, setOrder] = useState<Order>("desc");
  const [orderBy, setOrderBy] = useState<keyof MarketDataDto>("timestamp");
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [search, setSearch] = useState("");
  const isMobile = useMediaQuery("(max-width:600px)");

  // Real-time updates: poll every 10s if onRefresh provided
  useEffect(() => {
    if (!onRefresh) return;
    const interval = setInterval(onRefresh, 10000);
    return () => clearInterval(interval);
  }, [onRefresh]);

  const filteredData = useMemo(() => {
    if (!search) return data;
    return data.filter(
      (row) =>
        row.symbol.toLowerCase().includes(search.toLowerCase()) ||
        row.timestamp.toLowerCase().includes(search.toLowerCase())
    );
  }, [data, search]);

  const sortedData = useMemo(
    () => stableSort(filteredData, getComparator(order, orderBy)),
    [filteredData, order, orderBy]
  );

  const paginatedData = useMemo(
    () =>
      sortedData.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage),
    [sortedData, page, rowsPerPage]
  );

  const handleRequestSort = (property: keyof MarketDataDto) => {
    const isAsc = orderBy === property && order === "asc";
    setOrder(isAsc ? "desc" : "asc");
    setOrderBy(property);
  };

  const handleChangePage = (_: unknown, newPage: number) => setPage(newPage);
  const handleChangeRowsPerPage = (e: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(e.target.value, 10));
    setPage(0);
  };

  return (
    <Paper sx={{ width: "100%", overflow: "hidden", mb: 2 }}>
      <Toolbar
        sx={{
          display: "flex",
          flexDirection: isMobile ? "column" : "row",
          gap: 2,
        }}
      >
        <Typography variant="h6" sx={{ flex: 1 }}>
          Market Data
        </Typography>
        <TextField
          size="small"
          placeholder="Search symbol or date"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          InputProps={{
            startAdornment: <SearchIcon fontSize="small" sx={{ mr: 1 }} />,
          }}
          sx={{ minWidth: 200 }}
        />
        <Button
          variant="outlined"
          startIcon={<DownloadIcon />}
          onClick={() => exportToCSV(filteredData)}
        >
          Export CSV
        </Button>
      </Toolbar>
      <TableContainer sx={{ maxHeight: 500 }}>
        <Table stickyHeader size={isMobile ? "small" : "medium"}>
          <TableHead>
            <TableRow>
              {columns.map((col) => (
                <TableCell
                  key={col.id}
                  sortDirection={orderBy === col.id ? order : false}
                  align={col.numeric ? "right" : "left"}
                  style={{ minWidth: col.minWidth }}
                >
                  {col.numeric ? (
                    <TableSortLabel
                      active={orderBy === col.id}
                      direction={orderBy === col.id ? order : "asc"}
                      onClick={() =>
                        handleRequestSort(col.id as keyof MarketDataDto)
                      }
                    >
                      {col.label}
                    </TableSortLabel>
                  ) : (
                    col.label
                  )}
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              Array.from({ length: rowsPerPage }).map((_, i) => (
                <TableRow key={i}>
                  {columns.map((col) => (
                    <TableCell key={col.id}>
                      <Skeleton />
                    </TableCell>
                  ))}
                </TableRow>
              ))
            ) : paginatedData.length === 0 ? (
              <TableRow>
                <TableCell colSpan={columns.length} align="center">
                  <Typography variant="body2">No data found.</Typography>
                </TableCell>
              </TableRow>
            ) : (
              paginatedData.map((row) => (
                <TableRow hover key={row.id}>
                  <TableCell>{row.symbol}</TableCell>
                  <TableCell>
                    {new Date(row.timestamp).toLocaleString()}
                  </TableCell>
                  <TableCell align="right">
                    {formatCurrency(row.open)}
                  </TableCell>
                  <TableCell align="right">
                    {formatCurrency(row.high)}
                  </TableCell>
                  <TableCell align="right">{formatCurrency(row.low)}</TableCell>
                  <TableCell align="right">
                    {formatCurrency(row.close)}
                  </TableCell>
                  <TableCell align="right">
                    {row.volume.toLocaleString()}
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
      <TablePagination
        rowsPerPageOptions={[5, 10, 25, 50]}
        component="div"
        count={sortedData.length}
        rowsPerPage={rowsPerPage}
        page={page}
        onPageChange={handleChangePage}
        onRowsPerPageChange={handleChangeRowsPerPage}
      />
    </Paper>
  );
};

export default MarketDataTable;
