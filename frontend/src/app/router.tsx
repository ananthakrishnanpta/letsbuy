import { Route, Routes } from "react-router-dom";
import { AppShell } from "@/shared/components/AppShell";
import { ProtectedRoute } from "@/shared/components/ProtectedRoute";
import { AccountPage } from "@/features/auth/pages/AccountPage";
import { LoginPage } from "@/features/auth/pages/LoginPage";
import { RegisterPage } from "@/features/auth/pages/RegisterPage";
import { ForgotPasswordPage, ResetPasswordPage, VerifyEmailPage } from "@/features/auth/pages/RecoveryPages";
import { CatalogPage } from "@/features/catalog/pages/CatalogPage";
import { ProductPage } from "@/features/catalog/pages/ProductPage";
import { CartPage } from "@/features/cart/pages/CartPage";
import { CheckoutPage } from "@/features/ordering/pages/CheckoutPage";
import { OrderDetailPage } from "@/features/ordering/pages/OrderDetailPage";
import { OrdersPage } from "@/features/ordering/pages/OrdersPage";
import { AdminProductsPage } from "@/features/admin/pages/AdminProductsPage";

export function AppRouter() {
  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route index element={<CatalogPage />} />
        <Route path="products/:slug" element={<ProductPage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />
        <Route path="forgot-password" element={<ForgotPasswordPage />} />
        <Route path="reset-password" element={<ResetPasswordPage />} />
        <Route path="verify-email" element={<VerifyEmailPage />} />
        <Route path="cart" element={<ProtectedRoute><CartPage /></ProtectedRoute>} />
        <Route path="checkout" element={<ProtectedRoute><CheckoutPage /></ProtectedRoute>} />
        <Route path="orders" element={<ProtectedRoute><OrdersPage /></ProtectedRoute>} />
        <Route path="orders/:id" element={<ProtectedRoute><OrderDetailPage /></ProtectedRoute>} />
        <Route path="account" element={<ProtectedRoute><AccountPage /></ProtectedRoute>} />
        <Route path="admin/products" element={<ProtectedRoute admin><AdminProductsPage /></ProtectedRoute>} />
        <Route path="*" element={<h1 className="page-title">Page not found.</h1>} />
      </Route>
    </Routes>
  );
}
