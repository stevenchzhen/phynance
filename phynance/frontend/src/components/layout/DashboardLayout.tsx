import React, { useState } from "react";
import {
  AppBar,
  Toolbar,
  Typography,
  IconButton,
  Drawer,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  CssBaseline,
  Box,
  useTheme,
  ThemeProvider,
  createTheme,
  Menu,
  MenuItem,
  Switch,
  Breadcrumbs,
  Link,
  CircularProgress,
  Container,
} from "@mui/material";
import {
  Menu as MenuIcon,
  Dashboard,
  Assessment,
  AccountBalance,
  Settings,
  Brightness4,
  Brightness7,
  Logout,
  AccountCircle,
} from "@mui/icons-material";
import { useMediaQuery } from "@mui/material";

export interface DashboardLayoutProps {
  children: React.ReactNode;
  breadcrumbs?: { label: string; href?: string }[];
  loading?: boolean;
  error?: Error | null;
  onLogout?: () => void;
  userName?: string;
}

const drawerWidth = 220;

const navItems = [
  { label: "Dashboard", icon: <Dashboard />, href: "/dashboard" },
  { label: "Analysis", icon: <Assessment />, href: "/analysis" },
  { label: "Portfolio", icon: <AccountBalance />, href: "/portfolio" },
  { label: "Settings", icon: <Settings />, href: "/settings" },
];

const ColorModeContext = React.createContext({ toggleColorMode: () => {} });

function ErrorBoundary({ error }: { error: Error | null }) {
  if (!error) return null;
  return (
    <Box p={2} color="error.main">
      <Typography color="error">{error.message}</Typography>
    </Box>
  );
}

const DashboardLayout: React.FC<DashboardLayoutProps> = ({
  children,
  breadcrumbs,
  loading,
  error,
  onLogout,
  userName,
}) => {
  const [mobileOpen, setMobileOpen] = useState(false);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [mode, setMode] = useState<"light" | "dark">("light");
  const theme = createTheme({
    palette: {
      mode,
    },
  });
  const colorMode = React.useMemo(
    () => ({
      toggleColorMode: () =>
        setMode((prev) => (prev === "light" ? "dark" : "light")),
    }),
    []
  );
  const isMobile = useMediaQuery(theme.breakpoints.down("sm"));

  const handleDrawerToggle = () => setMobileOpen(!mobileOpen);
  const handleMenu = (event: React.MouseEvent<HTMLElement>) =>
    setAnchorEl(event.currentTarget);
  const handleClose = () => setAnchorEl(null);
  const handleLogout = () => {
    handleClose();
    onLogout && onLogout();
  };

  const drawer = (
    <Box sx={{ width: drawerWidth }} role="presentation">
      <Toolbar />
      <List>
        {navItems.map((item) => (
          <ListItem button key={item.label} component="a" href={item.href}>
            <ListItemIcon>{item.icon}</ListItemIcon>
            <ListItemText primary={item.label} />
          </ListItem>
        ))}
      </List>
    </Box>
  );

  return (
    <ColorModeContext.Provider value={colorMode}>
      <ThemeProvider theme={theme}>
        <Box
          sx={{
            display: "flex",
            minHeight: "100vh",
            bgcolor: "background.default",
            color: "text.primary",
          }}
        >
          <CssBaseline />
          <AppBar position="fixed" sx={{ zIndex: theme.zIndex.drawer + 1 }}>
            <Toolbar>
              {isMobile && (
                <IconButton
                  color="inherit"
                  edge="start"
                  onClick={handleDrawerToggle}
                  sx={{ mr: 2 }}
                >
                  <MenuIcon />
                </IconButton>
              )}
              <Typography variant="h6" sx={{ flexGrow: 1 }} noWrap>
                Phynance Analytics
              </Typography>
              <IconButton color="inherit" onClick={colorMode.toggleColorMode}>
                {mode === "dark" ? <Brightness7 /> : <Brightness4 />}
              </IconButton>
              <IconButton color="inherit" onClick={handleMenu} sx={{ ml: 1 }}>
                <AccountCircle />
              </IconButton>
              <Menu
                anchorEl={anchorEl}
                open={Boolean(anchorEl)}
                onClose={handleClose}
              >
                <MenuItem disabled>{userName || "User"}</MenuItem>
                <MenuItem onClick={handleLogout}>
                  <Logout fontSize="small" sx={{ mr: 1 }} />
                  Logout
                </MenuItem>
              </Menu>
            </Toolbar>
          </AppBar>
          <nav>
            {isMobile ? (
              <Drawer
                variant="temporary"
                open={mobileOpen}
                onClose={handleDrawerToggle}
                ModalProps={{ keepMounted: true }}
                sx={{
                  display: { xs: "block", sm: "none" },
                  "& .MuiDrawer-paper": {
                    boxSizing: "border-box",
                    width: drawerWidth,
                  },
                }}
              >
                {drawer}
              </Drawer>
            ) : (
              <Drawer
                variant="permanent"
                sx={{
                  display: { xs: "none", sm: "block" },
                  "& .MuiDrawer-paper": {
                    boxSizing: "border-box",
                    width: drawerWidth,
                  },
                }}
                open
              >
                {drawer}
              </Drawer>
            )}
          </nav>
          <Box
            component="main"
            sx={{
              flexGrow: 1,
              p: 3,
              width: { sm: `calc(100% - ${drawerWidth}px)` },
            }}
          >
            <Toolbar />
            {breadcrumbs && (
              <Breadcrumbs aria-label="breadcrumb" sx={{ mb: 2 }}>
                {breadcrumbs.map((crumb, idx) =>
                  crumb.href ? (
                    <Link
                      key={crumb.label}
                      color="inherit"
                      href={crumb.href}
                      underline="hover"
                    >
                      {crumb.label}
                    </Link>
                  ) : (
                    <Typography key={crumb.label} color="text.primary">
                      {crumb.label}
                    </Typography>
                  )
                )}
              </Breadcrumbs>
            )}
            {loading ? (
              <Box
                display="flex"
                justifyContent="center"
                alignItems="center"
                minHeight="200px"
              >
                <CircularProgress />
              </Box>
            ) : error ? (
              <ErrorBoundary error={error} />
            ) : (
              <Container maxWidth="xl" sx={{ mt: 2 }}>
                {children}
              </Container>
            )}
          </Box>
        </Box>
      </ThemeProvider>
    </ColorModeContext.Provider>
  );
};

export default DashboardLayout;
