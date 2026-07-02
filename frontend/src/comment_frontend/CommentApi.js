import axios from "axios";

const BASE_URL = `${(import.meta.env.VITE_API_BASE_URL || "http://localhost:8080").replace(/\/$/, "")}/api`;

const api = axios.create({
    baseURL: BASE_URL,
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem("accessToken");
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

const toData = (res) => ({ data: res.data?.data ?? res.data });

/* ----------------- 댓글 CRUD -------------------- */

// 댓글 작성
export const createComment = (data) => api.post("/comment", data).then(toData);

// 최상위 댓글만 조회 (대댓글 제외)
export const getTopLevelComments = (postId) => api.get(`/comment/post/${postId}/top`).then(toData);

// 대댓글 조회
export const getReplies = (parentCommentId) => api.get(`/comment/${parentCommentId}/replies`).then(toData);

// 댓글 수정
export const updateComment = (commentId, content) => api.put(`/comment/${commentId}`, { content }, {
    headers: { "Content-Type": "application/json" },
}).then(toData);

// 댓글 삭제
export const deleteComment = (commentId) => api.delete(`/comment/${commentId}`).then(toData);

// 게시글 댓글 수 조회
export const getCommentCount = (postId) => api.get(`/comment/post/${postId}/count`).then(toData);

/* ------------------ 좋아요 -------------------- */

// 좋아요
export const addCommentLike = (commentId, userId) => api.post(`/comment/${commentId}/likes/${userId}`).then(toData);

// 좋아요 취소
export const removeCommentLike = (commentId, userId) => api.delete(`/comment/${commentId}/likes/${userId}`).then(toData);

// 좋아요 수 조회
export const getCommentLikeCount = (commentId) => api.get(`/comment/${commentId}/likes/count`).then(toData);

// 좋아요 여부 확인
export const checkCommentLiked = (commentId, userId) => api.get(`/comment/${commentId}/likes/${userId}`).then(toData);

/* ------------------ 이미지 -------------------- */

// 이미지 업로드
export const uploadCommentImage = (commentId, file) => {
    const formData = new FormData();
    formData.append("file", file);
    return api.post(`/comment/${commentId}/images`, formData, {
        headers: {"Content-Type": "multipart/form-data"},
    })
    .then(toData);
};

// 댓글 이미지 전체 조회
export const getCommentImages = (commentId) => api.get(`/comment/${commentId}/images`).then(toData);

// 댓글 이미지 삭제
export const deleteCommentImage = (commentId, imageId) => api.delete(`/comment/${commentId}/images/${imageId}`).then(toData);

export default api
